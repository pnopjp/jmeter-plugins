package jp.co.pnop.jmeter.protocol.azureservicebus.common;

import java.util.concurrent.atomic.AtomicInteger;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential;
import jp.co.pnop.jmeter.protocol.amqp.util.AzAmqpProxyOptions;
import jp.co.pnop.jmeter.protocol.azureservicebus.config.AzServiceBusClient.AzServiceBusClientComponentImpl;

public class AzServiceBusClientParams extends AbstractTestElement {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusClientParams.class);

    /*
    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
        Arrays.asList(
            "jp.co.pnop.jmeter.protocol.azureservicebus.sampler.gui.AzServiceBusConfigGui",
            "org.apache.jmeter.config.gui.SimpleConfigGui"
        )
    );
    */

    public static final String CONNECTION_TYPE = "connectionType";
    public static final String NEW_CONNECTION = "newConnection";
    public static final String DEFINED_CONNECTION = "definedConnection";
    public static final String DEFINED_CONNECTION_NAME = "definedConnectionName";
    public static final String CONNECTION_NAME = "connectionName";
    public static final String NAMESPACE_NAME = "namespaceName";
    public static final String AUTH_TYPE = "authType";
    public static final String SHARED_ACCESS_KEY_NAME = "sharedAccessKeyName";
    public static final String SHARED_ACCESS_KEY = "sharedAccessKey";
    public static final String AAD_CREDENTIAL = "aadCredential";
    public static final String DEST_TYPE = "destType";
    public static final String QUEUE_NAME = "queueName";
    public static final String PROTOCOL = "protocol";

    public static final String CONNECTION_TYPE_NEW_CONNECTION = "Create New Connection";
    public static final String CONNECTION_TYPE_DEFINED_CONNECTION = "Use Defined Connection";
    public static final String CONNECTION_TYPE_DEFINED_TRANSACTION = "Use Defined Transaction";
    public static final String CONNECTION_TYPE_CONNECTION = "Connection";
    public static final String CONNECTION_TYPE_TRANSACTION = "Transaction";
    public static final String SERVICEBUS_CLIENT_PARAMS = "svcparams";


    public static final String AUTHTYPE_SAS = "Shared access signature";
    public static final String AUTHTYPE_AAD = "Azure AD credential";
    public static final String AUTHTYPE_ENTRAID = "Microsoft Entra ID credential";

    public static final String DEST_TYPE_QUEUE = "Queue";
    public static final String DEST_TYPE_TOPIC = "Topic";

    public static final String PROTOCOL_AMQP = "AMQP";
    public static final String PROTOCOL_AMQP_OVER_WEBSOCKETS = "AMQP over Web Sockets";

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    /**
     * Create a new Azure Service Bus Client object with no messages.
     */
    public AzServiceBusClientParams() {
        super();
        classCount.incrementAndGet();
        trace("AzServiceBusClientConfig()");
    }
    
    @Override
    public void clear() {
        super.clear();
        setProperty(new StringProperty(CONNECTION_TYPE, CONNECTION_TYPE_NEW_CONNECTION));
        setProperty(new StringProperty(DEFINED_CONNECTION_NAME, ""));
        setProperty(new StringProperty(CONNECTION_NAME, ""));
        setProperty(new StringProperty(NAMESPACE_NAME, ""));
        setProperty(new StringProperty(AUTH_TYPE, AUTHTYPE_SAS));
        setProperty(new StringProperty(SHARED_ACCESS_KEY_NAME, ""));
        setProperty(new StringProperty(SHARED_ACCESS_KEY, ""));
        setProperty(new StringProperty(AAD_CREDENTIAL, ""));
        setProperty(new StringProperty(DEST_TYPE, DEST_TYPE_QUEUE));
        setProperty(new StringProperty(QUEUE_NAME, ""));
        setProperty(new StringProperty(PROTOCOL, PROTOCOL_AMQP));
    }

    public String getConnectionType() {
        return getPropertyAsString(CONNECTION_TYPE);
    }

    public void setDefinedConnectionName(String definedConnectionName) {
        setProperty(new StringProperty(DEFINED_CONNECTION_NAME, definedConnectionName));
    }

    public String getDefinedConnectionName() {
        return getPropertyAsString(DEFINED_CONNECTION_NAME);
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

    public ServiceBusSenderClient getProducer() throws Exception {
        ServiceBusSenderClient producer = null;
        
        if (getConnectionType().equals(CONNECTION_TYPE_NEW_CONNECTION)) {
            ServiceBusClientBuilder producerBuilder = new ServiceBusClientBuilder();
            if (getAuthType().equals(AUTHTYPE_SAS)) {
                final String connectionString
                    = "Endpoint=sb://".concat(getNamespaceName()).concat("/;")
                    .concat("SharedAccessKeyName=").concat(getSharedAccessKeyName()).concat(";")
                    .concat("SharedAccessKey=").concat(getSharedAccessKey());
                producerBuilder.connectionString(connectionString);
            } else { // AUTHTYPE_ENTRAID or AUTHTYPE_AAD
                producerBuilder.credential(getNamespaceName(), (AzAdCredential.getCredential(getAadCredential())).getCredential());
            }

            AmqpTransportType protocol = null;
            if (getProtocol().equals(PROTOCOL_AMQP_OVER_WEBSOCKETS)) {
                protocol = AmqpTransportType.AMQP_WEB_SOCKETS;
                producerBuilder.proxyOptions(new AzAmqpProxyOptions().ProxyOptions());
            } else {
                protocol = AmqpTransportType.AMQP;
            }
            producerBuilder.transportType(protocol);

            if (getDestType().equals(DEST_TYPE_TOPIC)) {
                producer = producerBuilder.sender().topicName(getQueueName()).buildClient();
            } else {
                producer = producerBuilder.sender().queueName(getQueueName()).buildClient();
            }
            log.debug("Created connection: {}", producer.toString());
        } else { // CONNECTION_TYPE_DEFINED_CONNECTION or CONNECTION_TYPE_DEFINED_TRANSACTION
            Object tempObject = getThreadContext().getVariables().getObject(getDefinedConnectionName());
            if (!tempObject.getClass().getSimpleName().equals("AzServiceBusClientComponentImpl")) {
                throw new ClassCastException("The \"".concat(getDefinedConnectionName()).concat("\" you specified is not a connection."));
            }
            AzServiceBusClientComponentImpl serviceBusClient = (AzServiceBusClientComponentImpl) tempObject;
            producer = serviceBusClient.getProducer();
            log.debug("Get defined connection: {}", producer.toString());
        }

        return producer;
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
