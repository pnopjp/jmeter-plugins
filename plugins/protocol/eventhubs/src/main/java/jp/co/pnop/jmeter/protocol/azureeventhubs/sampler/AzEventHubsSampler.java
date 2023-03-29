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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

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
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.core.amqp.exception.*;

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

    private static final Map<String, ChronoUnit> chronoUnit = new HashMap<String, ChronoUnit>();
    static {
        chronoUnit.put ("MILLIS", ChronoUnit.MILLIS);
        chronoUnit.put ("SECONDS", ChronoUnit.SECONDS);
        chronoUnit.put ("MINUTES", ChronoUnit.MINUTES);
        chronoUnit.put ("HOURS", ChronoUnit.HOURS);
        chronoUnit.put ("DAYS", ChronoUnit.DAYS);
    };

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
            } else { // AUTHTYPE_AAD
                AzAdCredentialComponentImpl credential = AzAdCredential.getCredential(getAadCredential());
                requestBody = requestBody.concat(credential.getRequestBody());
                producerBuilder = producerBuilder.credential(getNamespaceName(), getEventHubName(), credential.getCredential());
            }
            producer = producerBuilder.buildProducerClient();

            // prepare a batch of events to send to the event hub
            CreateBatchOptions batchOptions = new CreateBatchOptions();
            if (getPartitionValue().length() > 0) {
                switch (getPartitionType()) {
                    case PARTITION_TYPE_ID:
                        batchOptions.setPartitionId(getPartitionValue());
                        requestBody = requestBody.concat("\n").concat("Partition ID: ").concat(getPartitionValue());
                        break;
                    case PARTITION_TYPE_KEY:
                        batchOptions.setPartitionKey(getPartitionValue());
                        requestBody = requestBody.concat("\n").concat("Partition Key: ").concat(getPartitionValue());
                        break;
                }
            }
            EventDataBatch batch = producer.createBatch(batchOptions);
    
            PropertyIterator iter = getMessages().iterator();
            int msgCount = 0;
            while (iter.hasNext()) {
                msgCount ++;
                AzAmqpMessage msg = (AzAmqpMessage) iter.next().getObjectValue();

                requestBody = requestBody.concat("\n\n")
                            .concat("[Event data #").concat(String.valueOf(msgCount)).concat("]\n")
                            .concat("Message type: ").concat(msg.getMessageType()).concat("\n")
                            .concat("Body: ").concat(msg.getMessage());
                EventData eventData;
                switch (msg.getMessageType()) {
                    case AzAmqpMessages.MESSAGE_TYPE_BASE64:
                        byte[] binMsg = Base64.getDecoder().decode(msg.getMessage().getBytes());
                        eventData = new EventData(binMsg);
                        break;
                    case AzAmqpMessages.MESSAGE_TYPE_FILE:
                        BufferedInputStream bi = null;
                        bi = new BufferedInputStream(new FileInputStream(msg.getMessage()));
                        eventData = new EventData(IOUtils.toByteArray(bi));
                        break;
                    case AzAmqpMessages.MESSAGE_TYPE_BYTES:
                        eventData = new EventData(msg.getMessage().getBytes());
                        break;
 
                    default: // AzAmqpMessages.MESSAGE_TYPE_STRING
                        eventData = new EventData(msg.getMessage());
                }

                String messageId = msg.getMessageId();
                if (!messageId.isEmpty()) {
                    eventData.setMessageId(messageId);
                    requestBody = requestBody.concat("\n").concat("Message ID: ").concat(messageId);
                }

                /*
                String partitionKey = msg.getPartitionKey();
                if (!partitionKey.isEmpty()) {
                    eventData.setPartitionKey(partitionKey);
                    requestBody = requestBody.concat("\n").concat("Partition Key: ").concat(partitionKey);
                }
                */

                String customProperties = msg.getCustomProperties();
                if (!customProperties.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> properties = mapper.readValue(customProperties, new TypeReference<Map<String, Object>>(){});
                    eventData.getProperties().putAll(properties);
                }

                String contentType = msg.getContentType();
                if (!contentType.isEmpty()) {
                    eventData.setContentType(contentType);
                    requestBody = requestBody.concat("\n").concat("Content Type: ").concat(contentType);
                }

                /*
                String label = msg.getLabel();
                if (!label.isEmpty()) {
                    eventData.setSubject(label);
                    requestBody = requestBody.concat("\n").concat("Label/Subject: ").concat(label);
                }
                */

                /*
                String standardProperties = msg.getStandardProperties();
                if (!standardProperties.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> properties = mapper.readValue(standardProperties, new TypeReference<Map<String, String>>(){});
                    for (Map.Entry<String, String> property : properties.entrySet()) {
                        switch (property.getKey().toLowerCase()) {
                            case "correlation-id":
                            case "correlationid":
                            eventData.setCorrelationId(property.getValue());
                            break;

                            case "reply-to":
                            case "replyto":
                            eventData.setReplyTo(property.getValue());
                            break;

                            case "reply-to-group-id":
                            case "replytosessionid":
                            eventData.setReplyToSessionId(property.getValue());
                            break;

                            case "to":
                            eventData.setTo(property.getValue());
                            break;

                            case "ttl":
                            case "timetolive":
                            Pattern pattern = Pattern.compile("([0-9]+)(.*)");
                            Matcher matcher = pattern.matcher(property.getValue());
                            if (matcher.find()) {
                                String unit;
                                if (matcher.group(2).trim().length() == 0) {
                                    unit = "SECONDS";
                                } else {
                                    unit = matcher.group(2).trim().toUpperCase();
                                }
                                try {
                                    eventData.setTimeToLive(Duration.of(Long.parseLong(matcher.group(1)), chronoUnit.get(unit)));
                                } catch (NullPointerException exTtl) {
                                    throw new Exception("Error calling ".concat(threadName).concat(":").concat(this.getName()).concat(". The \"").concat(property.getKey()).concat("\": \"").concat(property.getValue()).concat("\" in \"headers/properties/annotations\" was ignored. Only MILLIS, SECONDS, MINUTES, HOURS, and DAYS can be used for units."));
                                }
                            }
                            break;

                            case "x-opt-scheduled-enqueue-time":
                            case "scheduledenqueuetime":
                            eventData.setScheduledEnqueueTime(OffsetDateTime.parse(property.getValue()));
                            break;

                            default:
                            throw new Exception("Error calling ".concat(threadName).concat(":").concat(this.getName()).concat(". The \"").concat(property.getKey()).concat("\": \"").concat(property.getValue()).concat("\" in \"headers/properties/annotations\" was ignored."));
                        }
                    }
                    
                }
                */

                batch.tryAdd(eventData);
            }

            bytes = batch.getSizeInBytes();

            // send the batch of events to the event hub
            producer.send(batch);

            sentBytes = batch.getSizeInBytes();
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