package jp.co.pnop.jmeter.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.AbstractFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.util.httpclient.AzUtilHttpClient;

public class GetSecret extends AbstractFunction {
    
    private static final Logger log = LoggerFactory.getLogger(GetSecret.class);
    private static final String KEY = "__GetSecret";

    private static final List<String> desc = new ArrayList<String>();

    private static final int PARAM_SECRET_NAME = 0;

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 1;
    
    private static final String PLUGIN = "azure_load_testing_stub";
    private static final String JMPROPS_CATEGORY = PLUGIN.toLowerCase();
    private static final String JMPROPS_GET_SECRET = new StringBuilder(JMPROPS_CATEGORY).append(".get_secret.").toString();
    private static final String ENV_CATEGORY = PLUGIN.toUpperCase();
    private static final String ENV_GET_SECRET = new StringBuilder(ENV_CATEGORY).append("_GET_SECRET").toString();

    private static final String KEY_VAULT = "keyvault";
    private static final String JMETER_PROPERTIES = "jmeter_properties";
    private static final String ENVIRONMENT_VARIABLE = "environment_variable";
    private static final String ENVIRONMENT_VARIABLES = "environment_variables";

    private static final String STORE_TYPE = "store_type";
    private static final String AUTH_TYPE = "auth_type";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CLIENT_CERTIFICATE = "client_certificate";
    private static final String INTERACTIVE_BROWSER = "interactive_browser";
    private static final String MANAGED_ID = "managed_id";
    private static final String AZURE_CLI = "azure_cli";
    private static final String VS_CODE = "vs_code";

    private static final String CERTIFICATE_TYPE = "certificate_type";
    private static final String CERTIFICATE_FILE = "certificate_file";
    private static final String CERTIFICATE_PASSWORD = "certificate_password";
    private static final String VAULT_URI = "vault_uri";
    private static final String TENANT_ID = "tenant_id";
    private static final String CLIENT_ID = "client_id";
    private static final String SECRET_NAME = "secret_name";
    private static final String SECRET_VERSION = "secret_version";
    private static final String AUTHORITY_HOST = "authority_host";

    private static final String CLIENT_CERTIFICATE_PEM = "PEM";
    private static final String CLIENT_CERTIFICATE_PFX = "PFX";

    static {
        desc.add("Secret name");
    }

    private CompoundVariable[] values;

    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler) throws InvalidVariableException {
        Boolean configThread = false;
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste: stElements) {
            if (ste.getClassName().equals("org.apache.jmeter.config.Argument")) {
                configThread = true;
                break;
            }
        }
        if (!configThread) {
            return "";
        }
        
        String secretNameParam = values[PARAM_SECRET_NAME].execute().trim();
        String secret = "";
        String storeType =  JMeterUtils.getPropDefault(new StringBuilder(JMPROPS_GET_SECRET).append(secretNameParam).append(".").append(STORE_TYPE).toString(), "").trim().toLowerCase();

        try {
            switch (storeType) {
                //// KeyVault
                case KEY_VAULT:
                secret = getKeyVaultSecret(secretNameParam);
                break;

                //// jmeter.properties or user.properties
                case JMETER_PROPERTIES:
                String paramName = new StringBuilder(JMPROPS_GET_SECRET).append(secretNameParam).append(".value").toString();
                secret = JMeterUtils.getPropDefault(paramName, "");
                if (JOrphanUtils.isBlank(secret)) {
                    log.error("The parameter with the specified name has not been set in jmeter.properties or user.properties. ");
                    throw new GetSecretException(
                        new StringBuffer("The parameter with the specified name has not been set in jmeter.properties or user.properties. ")
                        .append("[").append(paramName).append("]")
                        .toString()
                    );
                }
                break;

                //// Environment variable
                case ENVIRONMENT_VARIABLE:
                case ENVIRONMENT_VARIABLES:
                String envName = new StringBuilder(ENV_GET_SECRET).append("_").append(secretNameParam).toString();
                secret = System.getenv(envName);
                if (JOrphanUtils.isBlank(secret)) {
                    throw new GetSecretException(
                        new StringBuffer("The environment variable with the specified name has not been set. ")
                        .append("[").append(envName).append("]")
                        .toString()
                    );
                }
                break;

                default:
                throw new GetSecretException(
                    new StringBuffer("The store type is not specified. ")
                    .append("Set ether ")
                    .append(KEY_VAULT).append(", ")
                    .append(JMETER_PROPERTIES).append(", ")
                    .append(ENVIRONMENT_VARIABLES).append(", ")
                    .append(MANAGED_ID).append(", ")
                    .append(INTERACTIVE_BROWSER).append(", ")
                    .append(AZURE_CLI).append(" or ")
                    .append(VS_CODE)
                    .append(" for ").append(JMPROPS_GET_SECRET).append(secretNameParam).append(".store_type in the jmeter.properties file or user.properties file.")
                    .toString()
                );
            }
            
            return secret;
        } catch (GetSecretException ex) {
            log.error("Error calling {} function with Secret name \"{}\". {}", KEY, secretNameParam, ex.getMessage(), ex);
            return "";
            //throw new InvalidVariableException(ex.getCause());
        } catch (Exception ex) {
            log.error("Error calling {} function with Secret name \"{}\". {}", KEY, secretNameParam, ex.toString(), ex);
            return "";
        }
    }

    private String getKeyVaultSecret(String secretNameParam) {
        TokenCredential credential = getCredential(secretNameParam);
        if (credential == null) {
            log.error("Failed to authenticate to Azure Key Vault. __GetSecret({})", secretNameParam);
            return "";
        }

        String vaultUrl = getKeyVaultProp(secretNameParam, VAULT_URI);
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(credential)
            .httpClient(AzUtilHttpClient.httpClientBase())
            .buildClient();

        String secretName = getKeyVaultProp(secretNameParam, SECRET_NAME);
        String secretVersion = getKeyVaultProp(secretNameParam, SECRET_VERSION);
        Response<KeyVaultSecret> response;
        response = secretClient.getSecretWithResponse(secretName, secretVersion, null);
        if (response.getStatusCode() != 200) {
            log.error("Failed to authenticate to Azure Key Vault. [__GetSecret({})]: {}", secretNameParam, response.getValue());
            return "";
        }
        KeyVaultSecret keyVaultSecret = response.getValue();

        return keyVaultSecret.getValue();
    }

    private TokenCredential getCredential(String secretNameParam) {
        String authType = getKeyVaultProp(secretNameParam, AUTH_TYPE).trim().toLowerCase();
        String tenantId = getKeyVaultProp(secretNameParam, TENANT_ID);
        String clientId = getKeyVaultProp(secretNameParam, CLIENT_ID);
        String authorityHost = getKeyVaultProp(secretNameParam, AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD).trim();
        switch (authorityHost.toLowerCase()) {
            case "azurechina":
            authorityHost = AzureAuthorityHosts.AZURE_CHINA;
            break;

            case "azuregermany":
            authorityHost = AzureAuthorityHosts.AZURE_GERMANY;
            break;

            case "azuregovernment":
            authorityHost = AzureAuthorityHosts.AZURE_GOVERNMENT;
            break;

            case "azurepubliccloud":
            authorityHost = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
            break;
        }

        switch (authType) {
            //// ClientSecretCredential
            case CLIENT_SECRET:
            String clientSecret = getKeyVaultProp(secretNameParam, CLIENT_SECRET);
            return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorityHost(authorityHost)
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();

            //// ClientCertificateCredential
            case CLIENT_CERTIFICATE:
            ClientCertificateCredentialBuilder clientCertificateCredentialBuilder = new ClientCertificateCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .authorityHost(authorityHost);
            String clientCertificateType = getKeyVaultProp(secretNameParam, CERTIFICATE_TYPE).trim().toUpperCase();
            String certificateFile = getKeyVaultProp(secretNameParam, CERTIFICATE_FILE);
            switch (clientCertificateType) {
                case CLIENT_CERTIFICATE_PEM:
                clientCertificateCredentialBuilder = clientCertificateCredentialBuilder.pemCertificate(certificateFile);
                break;

                case CLIENT_CERTIFICATE_PFX:
                String clientCertificatePassword = getKeyVaultProp(secretNameParam, CERTIFICATE_PASSWORD);
                clientCertificateCredentialBuilder = clientCertificateCredentialBuilder.pfxCertificate(certificateFile, clientCertificatePassword);
                break;
            }
            return clientCertificateCredentialBuilder
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();

            //// EnvironmentCredential
            case ENVIRONMENT_VARIABLE:
            case ENVIRONMENT_VARIABLES:
            return new EnvironmentCredentialBuilder()
                .authorityHost(authorityHost)
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();

            //// ManagedIdCredential
            case MANAGED_ID:
            ManagedIdentityCredentialBuilder managedIdCredentialBuilder = new ManagedIdentityCredentialBuilder();
            if (!JOrphanUtils.isBlank(clientId)) {
                managedIdCredentialBuilder = managedIdCredentialBuilder.clientId(clientId);
            }
            return managedIdCredentialBuilder
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();

            //// InteractiveBrowserCredential
            case INTERACTIVE_BROWSER:
            return new InteractiveBrowserCredentialBuilder()
                .authorityHost(authorityHost)
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();

            //// AzureCliCredential
            case AZURE_CLI:
            return new AzureCliCredentialBuilder()
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();

            //// VisualStudioCodeCredential
            case VS_CODE:
            VisualStudioCodeCredentialBuilder vStudioCodeCredentialBuilder = new VisualStudioCodeCredentialBuilder();
            if (!JOrphanUtils.isBlank(tenantId)) {
                vStudioCodeCredentialBuilder.tenantId(tenantId);
            }
            return vStudioCodeCredentialBuilder
                .httpClient(AzUtilHttpClient.httpClientBase())
                .build();
        }
        return null;
    }

    private String getKeyVaultProp(String secretName, String param) {
        return getKeyVaultProp(secretName, param, "");
    }

    private String getKeyVaultProp(String secretName, String param, String def) {
        return JMeterUtils.getPropDefault(new StringBuilder(JMPROPS_GET_SECRET).append(secretName).append(".").append(param).toString(), def).trim();
    }

    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
    }

    @Override
    public String getReferenceKey() {
        return KEY;
    }

    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    class GetSecretException extends Exception {
        private static final long serialVersionUID = 1L; 

        GetSecretException(String msg) {
            super(msg);
        }
    }
}
