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

package jp.co.pnop.jmeter.protocol.azureservicebus.sampler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.HashSet;

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

import com.azure.messaging.servicebus.*;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.exception.*;

import jp.co.pnop.jmeter.protocol.amqp.config.gui.AzAmqpMessage;
import jp.co.pnop.jmeter.protocol.amqp.config.gui.AzAmqpMessages;

import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential;
import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential.AzAdCredentialComponentImpl;

import jp.co.pnop.jmeter.protocol.amqp.util.AzAmqpProxyOptions;
import com.azure.core.amqp.ProxyOptions;

/**
 * Azure Service Bus Sampler (non-Bean version)
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
public class AzServiceBusSampler extends AbstractSampler implements TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
        Arrays.asList(
            "jp.co.pnop.jmeter.protocol.azureservicebus.sampler.gui.AzServiceBusConfigGui",
            "org.apache.jmeter.config.gui.SimpleConfigGui"
        )
    );

    public static final String NAMESPACE_NAME = "namespaceName";
    public static final String AUTH_TYPE = "authType";
    public static final String SHARED_ACCESS_KEY_NAME = "sharedAccessKeyName";
    public static final String SHARED_ACCESS_KEY = "sharedAccessKey";
    public static final String AAD_CREDENTIAL = "aadCredential";
    public static final String DEST_TYPE = "destType";
    public static final String QUEUE_NAME = "queueName";
    public static final String PROTOCOL = "protocol";
    public static final String MESSAGES = "messages";

    public static final String AUTHTYPE_SAS = "Shared access signature";
    public static final String AUTHTYPE_AAD = "Azure AD credential";

    public static final String DEST_TYPE_QUEUE = "Queue";
    public static final String DEST_TYPE_TOPIC = "Topic";

    public static final String PROTOCOL_AMQP = "AMQP";
    public static final String PROTOCOL_AMQP_OVER_WEBSOCKETS = "AMQP over Web Sockets";

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzServiceBusSampler() {
        super();
        classCount.incrementAndGet();
        trace("AzServiceBusSampler()");
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

    public void setDestType(String destType) {
        setProperty(new StringProperty(DEST_TYPE, destType));
    }

    public String getDestType() {
        return getPropertyAsString(DEST_TYPE);
    }

    public void setQueueName(String queueName) {
        setProperty(new StringProperty(QUEUE_NAME, queueName));
    }

    public String getQueueName() {
        return getPropertyAsString(QUEUE_NAME);
    }

    public void setProtocol(String protocol) {
        setProperty(new StringProperty(PROTOCOL, protocol));
    }

    public String getProtocol() {
        return getPropertyAsString(PROTOCOL);
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
        long bodySize = 0;
        long propertiesSize = 0;

        ServiceBusSenderClient producer = null;
        ServiceBusClientBuilder producerBuilder = new ServiceBusClientBuilder();

        try {
            res.sampleStart(); // Start timing
            requestBody
                = "Endpoint: sb://".concat(getNamespaceName()).concat("\n")
                .concat(getDestType()).concat(" name: ").concat(getQueueName());
            if (getAuthType() == AUTHTYPE_SAS) {
                final String connectionString
                    = "Endpoint=sb://".concat(getNamespaceName()).concat("/;")
                    .concat("SharedAccessKeyName=").concat(getSharedAccessKeyName()).concat(";")
                    .concat("SharedAccessKey=").concat(getSharedAccessKey());
                requestBody = requestBody.concat("\n")
                    .concat("Shared Access Policy: ").concat(getSharedAccessKeyName()).concat("\n")
                    .concat("Shared Access Key: **********");
                producerBuilder = producerBuilder.connectionString(connectionString);
            } else { // AUTHTYPE_AAD
                AzAdCredentialComponentImpl credential = AzAdCredential.getCredential(getAadCredential());
                requestBody = requestBody.concat(credential.getRequestBody());
                producerBuilder = producerBuilder.credential(getNamespaceName(), credential.getCredential());
            }

            AmqpTransportType protocol = null;
            if (getProtocol() == PROTOCOL_AMQP_OVER_WEBSOCKETS) {
                protocol = AmqpTransportType.AMQP_WEB_SOCKETS;
                producerBuilder = producerBuilder.proxyOptions(new AzAmqpProxyOptions().ProxyOptions());
                ProxyOptions proxyOptions = new AzAmqpProxyOptions().ProxyOptions();
                producerBuilder.proxyOptions(proxyOptions);
            } else {
                protocol = AmqpTransportType.AMQP;
            }
            producerBuilder = producerBuilder.transportType(protocol);

            if (getDestType().equals(AzServiceBusSampler.DEST_TYPE_TOPIC)) {
                producer = producerBuilder.sender().topicName(getQueueName()).buildClient();
            } else {
                producer = producerBuilder.sender().queueName(getQueueName()).buildClient();
            }
            
            ServiceBusMessageBatch batch = producer.createMessageBatch();
    
            PropertyIterator iter = getMessages().iterator();
            int msgCount = 0;
            while (iter.hasNext()) {
                msgCount ++;
                AzAmqpMessage msg = (AzAmqpMessage) iter.next().getObjectValue();

                requestBody = requestBody.concat("\n\n")
                            .concat("[Message #").concat(String.valueOf(msgCount)).concat("]");
                
                ServiceBusMessage serviceBusMessage = null;
                switch (msg.getMessageType()) {
                    case AzAmqpMessages.MESSAGE_TYPE_BASE64:
                        byte[] binMsg = Base64.getDecoder().decode(msg.getMessage().getBytes());
                        serviceBusMessage = new ServiceBusMessage(binMsg);
                        bodySize += binMsg.length;
                        break;
                    case AzAmqpMessages.MESSAGE_TYPE_FILE:
                        BufferedInputStream bi = null;
                        bi = new BufferedInputStream(new FileInputStream(msg.getMessage()));
                        serviceBusMessage = new ServiceBusMessage(IOUtils.toByteArray(bi));
                        break;
                    default: // AzAmqpMessages.MESSAGE_TYPE_STRING
                        serviceBusMessage = new ServiceBusMessage(msg.getMessage());
                        bodySize += msg.getMessage().getBytes("UTF-8").length;
                }

                String messageId = msg.getMessageId();
                if (!messageId.isEmpty()) {
                    serviceBusMessage.setMessageId(messageId);
                    requestBody = requestBody.concat("\n").concat("Message ID: ").concat(messageId);
                }

                String groupId = msg.getGroupId();
                if (!groupId.isEmpty()) {
                    serviceBusMessage.setSessionId(groupId);
                    requestBody = requestBody.concat("\n").concat("Session ID: ").concat(groupId);
                }

                String partitionKey = msg.getPartitionKey();
                if (!partitionKey.isEmpty()) {
                    serviceBusMessage.setPartitionKey(partitionKey);
                    requestBody = requestBody.concat("\n").concat("Partition Key: ").concat(partitionKey);
                }
                
                batch.tryAddMessage(serviceBusMessage);

                propertiesSize += 0;

                requestBody = requestBody.concat("\n")
                    .concat("Message type: ").concat(msg.getMessageType()).concat("\n")
                    .concat("Body: ").concat(msg.getMessage());
            }
    
            // send the batch of messages to the Service Bus
            producer.sendMessages(batch);
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
            res.setResponseData(ex.toString(), "UTF-8");
            res.setResponseCode(ex.getErrorCondition().getErrorCondition());
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
            res.setBodySize(bodySize);
            res.setHeadersSize((int)propertiesSize);
            res.setSentBytes(bodySize + propertiesSize);
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