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

import java.lang.ClassCastException;
import java.time.OffsetDateTime;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.messaging.servicebus.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.converters.time.OffsetDateTimeConverter;
import com.azure.core.amqp.exception.*;

import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessage;
import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessages;
import jp.co.pnop.jmeter.protocol.azureservicebus.common.AzServiceBusClientParams;

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
 */
public class AzServiceBusSampler extends AbstractSampler implements TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
        Arrays.asList(
            "org.apache.jmeter.config.gui.SimpleConfigGui"
        )
    );

    public static final String CREATE_TRANSACTION = "createTransaction";
    public static final String CREATE_TRANSACTION_NAME = "createTransacionName";
    public static final String CONTINUE_TRANSACTION = "continueTransaction";
    public static final String COMMIT_TRANSACTION = "commitTransaction";
    public static final String ROLLBACK_TRANSACTION = "rollbackTransaction";
    public static final String MESSAGES = "messages";

    class TransactionClass {
        private ServiceBusSenderClient producer;
        private ServiceBusTransactionContext transaction;

        TransactionClass(ServiceBusSenderClient prod, ServiceBusTransactionContext tran) {
            producer = prod;
            transaction = tran;
        }

        public ServiceBusSenderClient getProducer() {
            return producer;
        }

        public ServiceBusTransactionContext getTransaction() {
            return transaction;
        }

    }

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzServiceBusSampler() {
        super();
        classCount.incrementAndGet();
        trace("AzServiceBusSampler()");
    }

    /**
     * Clear the messages.
     */
    @Override
    public void clear() {
        super.clear();

        setProperty(new BooleanProperty(CREATE_TRANSACTION, false));
        setProperty(new StringProperty(CREATE_TRANSACTION_NAME, ""));
        setProperty(new BooleanProperty(CONTINUE_TRANSACTION, false));
        setProperty(new BooleanProperty(COMMIT_TRANSACTION, false));
        setProperty(new BooleanProperty(ROLLBACK_TRANSACTION, false));
        setProperty(new TestElementProperty(MESSAGES, null));
        setProperty(new TestElementProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS, null));
    }

    public void setServiceBusClientParams(AzServiceBusClientParams sbcParams) {
        setProperty(new TestElementProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS, sbcParams));
    }

    public AzServiceBusClientParams getServiceBusClientParams() {
        return (AzServiceBusClientParams) getProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS).getObjectValue();
    }

    public void setCreateTransaction(String createTransaction) {
        setProperty(new StringProperty(CREATE_TRANSACTION, createTransaction));
    }

    public Boolean getCreateTransaction() {
        return getPropertyAsBoolean(CREATE_TRANSACTION);
    }

    public void setCreateTransactionName(String createTransactionName) {
        setProperty(new StringProperty(CREATE_TRANSACTION_NAME, createTransactionName));
    }

    public String getCreateTransactionName() {
        return getPropertyAsString(CREATE_TRANSACTION_NAME);
    }

    public void setContinueTransaction(String continueTransaction) {
        setProperty(new StringProperty(CONTINUE_TRANSACTION, continueTransaction));
    }

    public Boolean getContinueTransaction() {
        return getPropertyAsBoolean(CONTINUE_TRANSACTION);
    }

    public void setCommitTransaction(String commitTransaction) {
        setProperty(new StringProperty(COMMIT_TRANSACTION, commitTransaction));
    }

    public Boolean getCommitTransaction() {
        return getPropertyAsBoolean(COMMIT_TRANSACTION);
    }

    public void setRollbackTransaction(String rollbackTransaction) {
        setProperty(new StringProperty(ROLLBACK_TRANSACTION, rollbackTransaction));
    }

    public Boolean getRollabckTransaction() {
        return getPropertyAsBoolean(ROLLBACK_TRANSACTION);
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
        long bodyBytes = 0;
        long bytes = 0;
        long sentBytes = 0;

        ServiceBusSenderClient producer = null;
        ServiceBusTransactionContext transaction = null;
        AzServiceBusClientParams serviceBusClientParams = getServiceBusClientParams();
        String connectionType = serviceBusClientParams.getConnectionType().toString();

        try {
            res.sampleStart(); // Start timing

            if (connectionType.equals(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION)) {
                String definedConnectionName = serviceBusClientParams.getDefinedConnectionName();
                Object tempObject;
                try {
                    tempObject = getThreadContext().getVariables().getObject(definedConnectionName);
                    if (!tempObject.getClass().getSimpleName().equals("TransactionClass")) {
                        throw new ClassCastException("The \"".concat(definedConnectionName).concat("\" you specified is not a transaction."));
                    }
                } catch (NullPointerException ex) {
                    throw new NullPointerException("Transaction \"".concat(definedConnectionName).concat("\" is not defined."));
                }
                TransactionClass tran = (TransactionClass) tempObject;

                transaction = tran.getTransaction();
                producer = tran.getProducer();
                log.debug("Get defined connection: {}", transaction.toString());
            } else { // CONNECTION_TYPE_NEW_CONNECTION or CONNECTION_TYPE_DEFINED_CONNECTION
                producer = serviceBusClientParams.getProducer();
            }
            requestBody
                = "Endpoint: sb://".concat(producer.getFullyQualifiedNamespace()).concat("\n")
                .concat("Queue/Topic name: ").concat(producer.getEntityPath());

            log.info("AzServiceBusSampler.sampler() createMessageBatch: {}", producer);
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
                        break;
                    case AzAmqpMessages.MESSAGE_TYPE_FILE:
                        BufferedInputStream bi = null;
                        bi = new BufferedInputStream(new FileInputStream(msg.getMessage()));
                        serviceBusMessage = new ServiceBusMessage(IOUtils.toByteArray(bi));
                        break;
                    default: // AzAmqpMessages.MESSAGE_TYPE_STRING
                        serviceBusMessage = new ServiceBusMessage(msg.getMessage());
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

                String customProperties = msg.getCustomProperties();
                if (!customProperties.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> properties = mapper.readValue(customProperties, new TypeReference<Map<String, Object>>(){});
                    serviceBusMessage.getApplicationProperties().putAll(properties);
                }

                String contentType = msg.getContentType();
                if (!contentType.isEmpty()) {
                    serviceBusMessage.setContentType(contentType);
                    requestBody = requestBody.concat("\n").concat("Content Type: ").concat(contentType);
                }

                String label = msg.getLabel();
                if (!label.isEmpty()) {
                    serviceBusMessage.setSubject(label);
                    requestBody = requestBody.concat("\n").concat("Label/Subject: ").concat(label);
                }

                String standardProperties = msg.getStandardProperties();
                if (!standardProperties.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, String> properties = mapper.readValue(standardProperties, new TypeReference<Map<String, String>>(){});
                    for (Map.Entry<String, String> property : properties.entrySet()) {
                        switch (property.getKey().toLowerCase()) {
                            case "correlationid":
                            case "correlation-id":
                            serviceBusMessage.setCorrelationId(property.getValue());
                            break;

                            case "replyto":
                            case "reply-to":
                            serviceBusMessage.setReplyTo(property.getValue());
                            break;

                            case "replytosessionid":
                            case "reply-to-group-id":
                            serviceBusMessage.setReplyToSessionId(property.getValue());
                            break;

                            case "scheduledenqueuetime":
                            case "absolute-expiry-time":
                            serviceBusMessage.setScheduledEnqueueTime(OffsetDateTime.parse(property.getValue()));
                            break;

                            case "to":
                            serviceBusMessage.setTo(property.getValue());
                            break;

                            default:
                            log.error("Error calling {} sampler. The \"{}\": \"{}\" in \"standard properties\" was ignored.", threadName, property.getKey(), property.getValue());
                        }
                    }
                    
                }

                batch.tryAddMessage(serviceBusMessage);
                bodyBytes += serviceBusMessage.getBody().toBytes().length;

                requestBody = requestBody.concat("\n")
                    .concat("Message type: ").concat(msg.getMessageType()).concat("\n")
                    .concat("Body: ").concat(msg.getMessage());
            }

            bytes = batch.getSizeInBytes();

            // send the batch of messages to the Service Bus
            if (connectionType.equals(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION)) {
                if (getRollabckTransaction()) {
                    producer.rollbackTransaction(transaction);
                    getThreadContext().getVariables().remove(serviceBusClientParams.getDefinedConnectionName());
                    transaction = null;

                    producer.sendMessages(batch);
                } else { // Continue transaction or Commit transaction
                    producer.sendMessages(batch, transaction);

                    if (getCommitTransaction()) {
                        producer.commitTransaction(transaction);
                        getThreadContext().getVariables().remove(serviceBusClientParams.getDefinedConnectionName());
                        transaction = null;
                    }
                }
            } else { // CONNECTION_TYPE_NEW_CONNECTION or CONNECTION_TYPE_DEFINED_CONNECTION
                if (getCreateTransaction()) {
                    String tranName = getCreateTransactionName();
                    if (JOrphanUtils.isBlank(tranName)) {
                        throw new Exception("Name for transaction must not be empty in " + getName());
                    } else if (getThreadContext().getVariables().getObject(tranName) != null) {
                        throw new Exception("Transaction already defined for ".concat(tranName));
                    } else {
                        transaction = producer.createTransaction();
                        getThreadContext().getVariables().putObject(getCreateTransactionName(), new TransactionClass(producer, transaction));
                    }
                    producer.sendMessages(batch, transaction);
                } else { // Don't create transaction
                    producer.sendMessages(batch);
                }
            }

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
        } catch (FileNotFoundException | ClassCastException | JsonParseException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            res.setResponseData(ex.getMessage(), "UTF-8");
            responseMessage = responseMessage.concat(ex.getMessage());
        } catch (ServiceBusException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            res.setResponseData(ex.toString(), "UTF-8");
            responseMessage = responseMessage.concat(ex.toString());
        } catch (Exception ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            res.setResponseData(ex.toString(), "UTF-8");
            responseMessage = responseMessage.concat(ex.toString());
        } finally {
            if (producer != null && connectionType == AzServiceBusClientParams.CONNECTION_TYPE_NEW_CONNECTION) {
                producer.close();
            }
            res.setSamplerData(requestBody); // Request Body
            res.setBytes(bytes);
            res.setBodySize(bodyBytes);
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
