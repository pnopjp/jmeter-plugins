package jp.co.pnop.jmeter.protocol.azurestoragequeue.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.storage.queue.*;

import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential;
import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential.AzAdCredentialComponentImpl;
import jp.co.pnop.jmeter.util.httpclient.AzUtilHttpClient;

public class AzStorageConnectionParams extends AbstractTestElement {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzStorageConnectionParams.class);

    public static final String AUTH_TYPE = "authType";
    public static final String CONNECTION_NAME = "connectionName";
    public static final String DEFAULT_ENDPOINTS_PROTOCOL = "defaultEndpointsProtocol";
    public static final String ACCOUNT_NAME = "accoutName";
    public static final String STORAGE_KEY = "storageKey";
    public static final String ENDPOINT_SUFFIX = "endpointSuffix";
    public static final String CONNECTION_STRING = "connectionString";
    public static final String AAD_CREDENTIAL = "aadCredential";
    public static final String QUEUE_NAME = "queueName";
    public static final String SAS_TOKEN = "sasToken";
    public static final String ENDPOINT_URL = "endpointUrl";

    public static final String STORAGE_CONNECTION_PARAMS = "storageConnectionParams";

    public static final String AUTHTYPE_CONNECTION_STRING = "Connection string";
    public static final String AUTHTYPE_KEY = "Storage Key";
    public static final String AUTHTYPE_SAS = "Shared access signature";
    public static final String AUTHTYPE_AAD = "Azure AD credential";

    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    /**
     * Create a new Azure Service Bus Client object with no messages.
     */
    public AzStorageConnectionParams() {
        super();
        classCount.incrementAndGet();
        trace("AzStorageConnectionParams()");
    }
    
    @Override
    public void clear() {
        super.clear();

        setProperty(new StringProperty(AUTH_TYPE, AUTHTYPE_CONNECTION_STRING));
        setProperty(new StringProperty(CONNECTION_NAME, ""));
        setProperty(new StringProperty(CONNECTION_STRING, ""));
        setProperty(new StringProperty(DEFAULT_ENDPOINTS_PROTOCOL, "https"));
        setProperty(new StringProperty(ACCOUNT_NAME, ""));
        setProperty(new StringProperty(STORAGE_KEY, ""));
        setProperty(new StringProperty(ENDPOINT_SUFFIX, "core.windows.net"));
        setProperty(new StringProperty(AAD_CREDENTIAL, ""));
        setProperty(new StringProperty(QUEUE_NAME, ""));
        setProperty(new StringProperty(SAS_TOKEN, ""));
        setProperty(new StringProperty(ENDPOINT_URL, ""));
    }

    public void setAuthType(String authType) {
        setProperty(new StringProperty(AUTH_TYPE, authType));
    }

    public String getAuthType() {
        return getPropertyAsString(AUTH_TYPE);
    }

    public void setConnectionString(String connectionString) {
        setProperty(new StringProperty(CONNECTION_STRING, connectionString));
    }

    public String getConnectionString() {
        return getPropertyAsString(CONNECTION_STRING);
    }
    
    public void setDefaultEndpointsProtocol(String defaultEndpointsProtocol) {
        setProperty(new StringProperty(DEFAULT_ENDPOINTS_PROTOCOL, defaultEndpointsProtocol));
    }

    public String getDefaultEndpointsProtocol() {
        return getPropertyAsString(DEFAULT_ENDPOINTS_PROTOCOL);
    }
    
    public void setAccountName(String accountName) {
        setProperty(new StringProperty(ACCOUNT_NAME, accountName));
    }

    public String getAccountName() {
        return getPropertyAsString(ACCOUNT_NAME);
    }

    public void setStorageKey(String storageKey) {
        setProperty(new StringProperty(STORAGE_KEY, storageKey));
    }

    public String getStorageKey() {
        return getPropertyAsString(STORAGE_KEY);
    }

    public void setEndpointSuffix(String endpointSuffix) {
        setProperty(new StringProperty(ENDPOINT_SUFFIX, endpointSuffix));
    }

    public String getEndpointSuffix() {
        return getPropertyAsString(ENDPOINT_SUFFIX);
    }

    public void setAadCredential(String aadCredential) {
        setProperty(new StringProperty(AAD_CREDENTIAL, aadCredential));
    }

    public String getAadCredential() {
        return getPropertyAsString(AAD_CREDENTIAL);
    }

    public void setQueueName(String queueName) {
        setProperty(new StringProperty(QUEUE_NAME, queueName));
    }

    public String getQueueName() {
        return getPropertyAsString(QUEUE_NAME);
    }

    public void setSasToken(String sasToken) {
        setProperty(new StringProperty(SAS_TOKEN, sasToken));
    }

    public String getSasToken() {
        return getPropertyAsString(SAS_TOKEN);
    }

    public void setEndpointUrl(String endpointUrl) {
        setProperty(new StringProperty(ENDPOINT_URL, endpointUrl));
    }

    public String getEndpointUrl() {
        return getPropertyAsString(ENDPOINT_URL);
    }

    public String getMaskedParams() throws Exception {
        String authType = getAuthType();
        String params = "";

        if (authType.equals(AUTHTYPE_CONNECTION_STRING) || authType.equals(AUTHTYPE_KEY)) {
            String maskedConnectionString = getConnectionString()
                .replaceAll("(SharedAccessSignature=)[^;]*([;$])", "$1********$2")
                .replaceAll("(AccountKey=)[^;]*([;$])", "$1********$2");
            params = params.concat("Connection string: ").concat(maskedConnectionString).concat("\n");
            if (!getQueueName().isBlank()) {
                params = params.concat("Queue name: ").concat(getQueueName()).concat("\n");
            }
        } else { // AUTHTYPE_SAS or AUTHTYPE_AAD
            String maskedEndpointUrl = getEndpointUrl()
                .replaceAll("([\\?&]sv=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]ss=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]srt=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]sp=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]se=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]st=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]spr=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]si=)[^\\?&]*", "$1****")
                .replaceAll("([\\?&]sig=)[^\\?&]*", "$1********");
            params = params.concat("Endpoint Url: ").concat(maskedEndpointUrl).concat("\n");
            if (!getQueueName().isBlank()) {
                params = params.concat("Queue name: ").concat(getQueueName()).concat("\n");
            }
            if (authType.equals(AUTHTYPE_SAS)) {
                if (!getSasToken().isBlank()) {
                    params = params.concat("SAS token: ********\n");
                }
            } else if (authType.equals(AUTHTYPE_AAD)) {
                params = params.concat("Credential name: ").concat(getAadCredential()).concat("\n");
            }
        }

        return params;
    }

    public QueueClient getConnection() throws Exception {
        QueueClientBuilder queueClientBuilder = new QueueClientBuilder();
        QueueClient queueClient = null;

        String authType = getAuthType();

        if (authType.equals(AUTHTYPE_CONNECTION_STRING) || authType.equals(AUTHTYPE_KEY)) {
            queueClientBuilder = queueClientBuilder.connectionString(getConnectionString());
            if (!getQueueName().isBlank()) {
                queueClientBuilder = queueClientBuilder.queueName(getQueueName());
            }
        } else if (authType.equals(AUTHTYPE_SAS)) {
            queueClientBuilder = queueClientBuilder.endpoint(getEndpointUrl());
            if (!getQueueName().isBlank()) {
                queueClientBuilder = queueClientBuilder.queueName(getQueueName());
            }
            if (!getSasToken().isBlank()) {
                queueClientBuilder = queueClientBuilder.sasToken(getSasToken());
            }
        } else if (authType.equals(AUTHTYPE_AAD)) {
            queueClientBuilder = queueClientBuilder.endpoint(getEndpointUrl());
            if (!getQueueName().isBlank()) {
                queueClientBuilder = queueClientBuilder.queueName(getQueueName());
            }
            AzAdCredentialComponentImpl credential = AzAdCredential.getCredential(getAadCredential());
            queueClientBuilder = queueClientBuilder.credential(credential.getCredential());
        }

        queueClientBuilder =  queueClientBuilder.httpClient(AzUtilHttpClient.httpClientBase());
        queueClient = queueClientBuilder.buildClient();
        log.debug("Created connection: {}", queueClient.toString());

        return queueClient;
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
