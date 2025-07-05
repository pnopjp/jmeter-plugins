/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.pnop.jmeter.protocol.azureeventhubs.sampler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.HashSet;
//import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.PropertyIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.models.SendOptions;
import com.azure.core.amqp.exception.AmqpException;

import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential;
import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential.AzAdCredentialComponentImpl;
import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessage;
import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessages;

/**
 * Azure Event Hubs Sampler (non-Bean version)
 * <p>
 * JMeter creates an instance of a sampler class for every occurrence of the
 * element in every thread. [some additional copies may be created before the
 * test run starts]
 * <p>
 * Thus each sampler is guaranteed to be called by a single thread - there is no
 * need to synchronize access to instance variables.
 * <p>
 * However, access to class fields must be synchronized.
 *
 */
public class AzEventHubsSampler extends AbstractSampler implements TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzEventHubsSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
        Arrays.asList(
            "org.apache.jmeter.config.gui.SimpleConfigGui"
        )
    );

    public static final String NAMESPACE_NAME = "namespaceName";
    public static final String AUTH_TYPE = "authType";
    public static final String SHARED_ACCESS_KEY_NAME = "sharedAccessKeyName";
    public static final String SHARED_ACCESS_KEY = "sharedAccessKey";
    public static final String AAD_CREDENTIAL = "aadCredential";
    public static final String EVENT_HUB_NAME = "eventHubName";
    public static final String PARTITION_TYPE = "partitionType";
    public static final String PARTITION_VALUE = "partitionValue";
    public static final String MESSAGES = "messages";

    public static final String AUTHTYPE_SAS = "Shared access signature";
    public static final String AUTHTYPE_AAD = "Azure AD credential";
    public static final String AUTHTYPE_ENTRAID = "Microsoft Entra ID credential";

    public static final String PARTITION_TYPE_NOT_SPECIFIED = "Not specified";
    public static final String PARTITION_TYPE_ID = "ID";
    public static final String PARTITION_TYPE_KEY = "Key";

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzEventHubsSampler() {
        super();
        classCount.incrementAndGet();
        trace("AzEventHubsSampler()");
    }

    public void setNamespaceName(String namespaceName) {
        setProperty(new StringProperty(NAMESPACE_NAME, namespaceName));
    }

    public String getNamespaceName() {
        return getPropertyAsString(NAMESPACE_NAME);
    }

    public void setAuthType(String authType) {
        setProperty(new StringProperty(AUTH_TYPE, authType));
    }

    public String getAuthType() {
        return getPropertyAsString(AUTH_TYPE);
    }

    public void setSharedAccessKeyName(String sharedAccessKeyName) {
        setProperty(new StringProperty(SHARED_ACCESS_KEY_NAME, sharedAccessKeyName));
    }

    public String getSharedAccessKeyName() {
        return getPropertyAsString(SHARED_ACCESS_KEY_NAME);
    }

    public void setSharedAccessKey(String sharedAccessKey) {
        setProperty(new StringProperty(SHARED_ACCESS_KEY, sharedAccessKey));
    }

    public String getSharedAccessKey() {
        return getPropertyAsString(SHARED_ACCESS_KEY);
    }

    public void setAadCredential(String aadCredential) {
        setProperty(new StringProperty(AAD_CREDENTIAL, aadCredential));
    }

    public String getAadCredential() {
        return getPropertyAsString(AAD_CREDENTIAL);
    }

    public void setEventHubName(String eventHubName) {
        setProperty(new StringProperty(EVENT_HUB_NAME, eventHubName));
    }

    public String getEventHubName() {
        return getPropertyAsString(EVENT_HUB_NAME);
    }

    public void setPartitionType(String partitionType) {
        setProperty(new StringProperty(PARTITION_TYPE, partitionType));
    }

    public String getPartitionType() {
        return getPropertyAsString(PARTITION_TYPE);
    }

    public void setPartitionValue(String partitionValue) {
        setProperty(new StringProperty(PARTITION_VALUE, partitionValue));
    }

    public String getPartitionValue() {
        return getPropertyAsString(PARTITION_VALUE).trim();
    }

    public void setMessages(AzAmqpMessages messages) {
        setProperty(new TestElementProperty(MESSAGES, messages));
    }

    public AzAmqpMessages getMessages() {
        return (AzAmqpMessages) getProperty(MESSAGES).getObjectValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        trace("sample()");
        boolean isSuccessful = false;

        SampleResult res = new SampleResult();
        res.setSampleLabel(this.getName());

        String threadName = Thread.currentThread().getName();
        String responseMessage = "";
        String requestBody = "";
        long bytes = 0;
        long sentBytes = 0;

        EventHubProducerClient producer = null;
        EventHubClientBuilder producerBuilder = new EventHubClientBuilder();

        try {
            res.sampleStart(); // Start timing
            requestBody
                = "Endpoint: sb://".concat(getNamespaceName()).concat("\n")
                .concat("Event Hub: ").concat(getEventHubName());

            if (getAuthType().equals(AUTHTYPE_SAS)) {
                final String connectionString
                    = "Endpoint=sb://".concat(getNamespaceName()).concat("/;")
                    .concat("SharedAccessKeyName=").concat(getSharedAccessKeyName()).concat(";")
                    .concat("SharedAccessKey=").concat(getSharedAccessKey());
                requestBody = requestBody.concat("\n")
                    .concat("Shared Access Policy: ").concat(getSharedAccessKeyName()).concat("\n")
                    .concat("Shared Access Key: **********");
                producerBuilder = producerBuilder.connectionString(connectionString, getEventHubName());
            } else { // AUTHTYPE_ENTRAID or AUTHTYPE_AAD
                AzAdCredentialComponentImpl credential = AzAdCredential.getCredential(getAadCredential());
                requestBody = requestBody.concat(credential.getRequestBody());
                producerBuilder = producerBuilder.credential(getNamespaceName(), getEventHubName(), credential.getCredential());
            }
            producer = producerBuilder.buildProducerClient();

            // prepare events to send to the event hub
            SendOptions sendOptions = new SendOptions();
            if (getPartitionValue().length() > 0) {
                switch (getPartitionType()) {
                    case PARTITION_TYPE_ID:
                        sendOptions.setPartitionId(getPartitionValue());
                        requestBody = requestBody.concat("\n").concat("Partition ID: ").concat(getPartitionValue());
                        break;
                    case PARTITION_TYPE_KEY:
                        sendOptions.setPartitionKey(getPartitionValue());
                        requestBody = requestBody.concat("\n").concat("Partition Key: ").concat(getPartitionValue());
                        break;
                }
            }
            List<EventData> events = new ArrayList<>();

            PropertyIterator iter = getMessages().iterator();
            int msgCount = 0;
            while (iter.hasNext()) {
                msgCount ++;
                AzAmqpMessage msg = (AzAmqpMessage) iter.next().getObjectValue();
                String shortedMessage = "";

                EventData eventData;
                switch (msg.getMessageType()) {
                    case AzAmqpMessages.MESSAGE_TYPE_BASE64:
                        byte[] binMsg = Base64.getDecoder().decode(msg.getMessage().getBytes());
                        eventData = new EventData(binMsg);
                        if (msg.getMessage().length() > 8) {
                            shortedMessage = "BASE64: ".concat(msg.getMessage().substring(0, 8)).concat("...");
                        } else {
                            shortedMessage = "BASE64: ".concat(msg.getMessage());
                        }
                        break;
                    case AzAmqpMessages.MESSAGE_TYPE_FILE:
                        BufferedInputStream bi = null;
                        bi = new BufferedInputStream(new FileInputStream(msg.getMessage()));
                        eventData = new EventData(IOUtils.toByteArray(bi));
                        shortedMessage = "FILE: ".concat(msg.getMessage());
                        break;
                    default: // AzAmqpMessages.MESSAGE_TYPE_STRING
                        eventData = new EventData(msg.getMessage());
                        if (msg.getMessage().length() > 12) {
                            shortedMessage = msg.getMessage().substring(0, 12).concat("...");
                        } else {
                            shortedMessage = msg.getMessage();
                        }
                }
                requestBody = requestBody.concat("\n\n")
                            .concat("[Event data #").concat(String.valueOf(msgCount)).concat("]\n")
                            .concat("Message type: ").concat(msg.getMessageType()).concat("\n")
                            .concat("Body: ").concat(shortedMessage);

                events.add(eventData);
                bytes += eventData.getBody().length;
            }

            // send the events to the event hub
            producer.send(events, sendOptions);

            sentBytes = bytes;
            res.latencyEnd();

            res.setDataType(SampleResult.TEXT);

            //res.setResponseHeaders();
            //res.setResponseMessage();
            //res.setResponseCodeOK();
            responseMessage = "OK";
            isSuccessful = true;
            res.sampleEnd(); // End timing
        } catch (AmqpException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            if (ex.isTransient()) {
                responseMessage = "A transient error occurred in ".concat(threadName).concat(" sampler. Please try again later.\n");
            }
            responseMessage = responseMessage.concat(ex.getMessage());
            res.setResponseData(ex.getMessage(), "UTF-8");
        } catch (FileNotFoundException ex) {
            res.setResponseData(ex.toString(), "UTF-8");
            responseMessage = ex.getMessage();
            log.info("Error calling {} sampler. ", threadName, ex);
        } catch (Exception ex) {
            res.setResponseData(ex.toString(), "UTF-8");
            responseMessage = ex.getMessage();
            log.info("Error calling {} sampler. ", threadName, ex);
        } finally {
            if (producer != null) {
                producer.close();
            }
            res.setSamplerData(requestBody); // Request Body
            res.setBytes(bytes);
            res.setSentBytes(sentBytes);
            res.setResponseMessage(responseMessage);
        }

        res.setSuccessful(isSuccessful);
        return res;
    }

    @Override
    public void testStarted() {
        testStarted(""); // $NON-NLS-1$
    }

    @Override
    public void testEnded() {
        testEnded(""); // $NON-NLS-1$
    }

    @Override
    public void testStarted(String host) {
        // ignored
    }

    // Ensure any remaining contexts are closed
    @Override
    public void testEnded(String host) {

    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    /*
     * Helper method
     */
    private void trace(String s) {
        if (log.isDebugEnabled()) {
            log.debug("{} ({}) {} {} {}", Thread.currentThread().getName(), classCount.get(),
                    this.getName(), s, this.toString());
        }
    }
}