package jp.co.pnop.jmeter.protocol.azureservicebus.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;

import com.azure.messaging.servicebus.*;

import jp.co.pnop.jmeter.protocol.azureservicebus.common.AzServiceBusClientParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzServiceBusClient extends ConfigTestElement implements TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusClient.class);
    
    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    private transient ServiceBusSenderClient producer = null;
    private transient ServiceBusTransactionContext transaction = null;

    public AzServiceBusClient() {
        classCount.incrementAndGet();
        trace("AzServiceBusCrteateTransaction()");
    }

    public void setConnectionName(String connectionName) {
        setProperty(new StringProperty(AzServiceBusClientParams.CONNECTION_NAME, connectionName));
    }

    public String getConnectionName() {
        return getPropertyAsString(AzServiceBusClientParams.CONNECTION_NAME);
    }

    public void setServiceBusClientParams(AzServiceBusClientParams sbcParams) {
        setProperty(new TestElementProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS, sbcParams));
    }

    public AzServiceBusClientParams getServiceBusClient() {
        return (AzServiceBusClientParams) getProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS).getObjectValue();
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle() {
        return this.getName();
    }

    /**
     * Clear the messages.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new StringProperty(AzServiceBusClientParams.CONNECTION_NAME, ""));
        setProperty(new TestElementProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS, null));
    }

    @Override
    public void testEnded() {
        synchronized (this) {
            if (transaction != null) {
                producer.rollbackTransaction(transaction);
            }
            if (producer != null) {
                producer.close();
                producer = null;
            }
        }
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }

    @Override
    public void testStarted() {
        this.setRunningVersion(true);
        JMeterVariables variables = getThreadContext().getVariables();

        String clientName = getConnectionName();
        if (JOrphanUtils.isBlank(clientName)) {
            log.error("Name for connection/transaction must not be empty in " + getName());
        } else if (variables.getObject(clientName) != null) {
            log.error("Connection already defined for {}", clientName);
        } else {
            AzServiceBusClientComponentImpl client = new AzServiceBusClientComponentImpl();
            variables.putObject(clientName, client);
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public Object clone() {
        AzServiceBusClient client = (AzServiceBusClient) super.clone();
        synchronized (this) {
            client.producer = producer;
            client.transaction = transaction;
        }
        return client;
    }

    public static AzServiceBusClientComponentImpl getServiceBusClient(String clientName) throws Exception {
        Object clientObject = JMeterContextService.getContext().getVariables().getObject(clientName);
        if (clientObject == null) {
            throw new Exception("No transaction found named: '" + clientName + "', ensure Variable Name matches Variable Name of Transaction.");
        } else {
            if (clientObject instanceof AzServiceBusClientComponentImpl) {
                AzServiceBusClientComponentImpl client = (AzServiceBusClientComponentImpl) clientObject;
                return client;
            } else {
                String errorMsg = "Found object stored under variable:'" + clientName + "' with class:"
                                + clientObject.getClass().getName() + "and value: '" + clientObject
                                + " but it's not a AzServiceBusTransactionComponent, check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new Exception(errorMsg);
            }
        }
    }

    public class AzServiceBusClientComponentImpl {
        private transient AzServiceBusClientParams serviceBusClient = null;

        private String requestBody = "";

        AzServiceBusClientComponentImpl() {
            if (serviceBusClient == null) {
                serviceBusClient = getServiceBusClient();
            }

            try {
                producer = serviceBusClient.getProducer();
            } catch (Exception ex) {
                log.error("Create connection error: {}", ex.getMessage(), ex);
            }
        }

        public ServiceBusSenderClient getProducer() {
            return producer;
        }

        public void rollbackTransaction() {
            producer.rollbackTransaction(transaction);
        }

        public void close() {
            producer.close();
        }

        public String getRequestBody() {
            return requestBody;
        }
    }

    @Override
    public boolean expectsModification() {
        return false;
    }

    /*
     * Helper method
     */
    private void trace(String s) {
        if (log.isDebugEnabled()) {
            log.debug("{} ({}) {} {} {}", Thread.currentThread().getName(), classCount.get(),
                    getTitle(), s, this.toString());
        }
    }
}
