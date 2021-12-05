package jp.co.pnop.jmeter.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.print.attribute.standard.JobHoldUntil;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
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

    private static final String STORE_TYPE = "store_type";
    private static final String AUTH_TYPE = "auth_type";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String CLIENT_CERTIFICATE = "client_certificate";
    private static final String INTERACTIVE_BROWSER = "web_browser";
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
        TokenCredential credential = null;
        String secretNameParam = values[PARAM_SECRET_NAME].execute().trim();
        String secret = "";
        String storeType =  JMeterUtils.getPropDefault(new StringBuilder(JMPROPS_GET_SECRET).append(secretNameParam).append(".").append(STORE_TYPE).toString(), "").trim().toLowerCase();

        try {
            switch (storeType) {
                //// KeyVault
                case KEY_VAULT:
                String authType = getKeyVaultProp(secretNameParam, AUTH_TYPE).trim().toLowerCase();
                String tenantId = getKeyVaultProp(secretNameParam, TENANT_ID);
                String clientId = getKeyVaultProp(secretNameParam, CLIENT_ID);
                String authorityHost = getKeyVaultProp(secretNameParam, AUTHORITY_HOST, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
                switch (authorityHost) {
                    case "AzureChina":
                    authorityHost = AzureAuthorityHosts.AZURE_CHINA;
                    break;

                    case "AzureGermany":
                    authorityHost = AzureAuthorityHosts.AZURE_GERMANY;
                    break;

                    case "AzureGovernment":
                    authorityHost = AzureAuthorityHosts.AZURE_GOVERNMENT;
                    break;

                    case "AzurePublicCloud":
                    authorityHost = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
                    break;
                }

                switch (authType) {
                    //// ClientSecretCredential
                    case CLIENT_SECRET:
                    String clientSecret = getKeyVaultProp(secretNameParam, CLIENT_SECRET);
                    credential = new ClientSecretCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .authorityHost(authorityHost)
                        .build();
                    break;

                    //// ClientCertificateCredential
                    case CLIENT_CERTIFICATE:
                    ClientCertificateCredentialBuilder clientCertificateCredentialBuilder = new ClientCertificateCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .authorityHost(authorityHost);
                    String clientCertificateType = getKeyVaultProp(secretNameParam, CERTIFICATE_TYPE).trim().toUpperCase();
                    String certificateFile = getKeyVaultProp(secretNameParam, CERTIFICATE_FILE);
                    if (clientCertificateType.equals(CLIENT_CERTIFICATE_PEM)) {
                        clientCertificateCredentialBuilder = clientCertificateCredentialBuilder.pemCertificate(certificateFile);
                    } else { // CLIENT_CERTIFICATE_PFX
                        String clientCertificatePassword = getKeyVaultProp(secretNameParam, CERTIFICATE_PASSWORD);
                        clientCertificateCredentialBuilder = clientCertificateCredentialBuilder.pfxCertificate(certificateFile, clientCertificatePassword);
                    }
                    credential = clientCertificateCredentialBuilder.build();
                    break;

                    //// EnvironmentCredential
                    case ENVIRONMENT_VARIABLE:
                    credential = new EnvironmentCredentialBuilder().authorityHost(authorityHost).build();
                    break;

                    //// ManagedIdCredential

                    //// DeviceCodeCredential

                    //// InteractiveBrowserCredential
                    case INTERACTIVE_BROWSER:
                    credential = new InteractiveBrowserCredentialBuilder().authorityHost(authorityHost).build();
                    break;

                    //// UsernamePasswordCredential

                }

                String vaultUrl = getKeyVaultProp(secretNameParam, VAULT_URI);
                SecretClient secretClient = new SecretClientBuilder()
                    .vaultUrl(vaultUrl)
                    .credential(credential)
                    .buildClient();

                String secretName = getKeyVaultProp(secretNameParam, SECRET_NAME);
                String secretVersion = getKeyVaultProp(secretNameParam, SECRET_VERSION);
                KeyVaultSecret keyVaultSecret;
                if (JOrphanUtils.isBlank(secretVersion)) {
                    keyVaultSecret = secretClient.getSecret(secretName);
                } else {
                    keyVaultSecret = secretClient.getSecret(secretName, secretVersion);
                }
                secret = keyVaultSecret.getValue();
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
                    .append("Set ether ").append(KEY_VAULT).append(", ").append(JMETER_PROPERTIES).append(", ").append(ENVIRONMENT_VARIABLE)
                    .append(" for ").append(JMPROPS_GET_SECRET).append(secretNameParam).append(".store_type in the jmeter.properties file or user.properties file.")
                    .toString()
                );
            }
            
            return secret;
        } catch (GetSecretException ex) {
            log.error(new StringBuilder("Error calling {} function with Secret name \"{}\". ").append(ex.getMessage()).toString(), KEY, secretNameParam, ex);
            throw new InvalidVariableException(ex.getCause());
        }
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
