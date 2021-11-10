package jp.co.pnop.jmeter.protocol.aad.config;


import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.threads.JMeterContextService;

import com.azure.core.credential.TokenCredential;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jmeter.testelement.TestStateListener;
import java.net.InetSocketAddress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.InteractiveBrowserCredentialBuilder;

import org.apache.jmeter.util.JMeterUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzAdCredential extends ConfigTestElement implements TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzAdCredential.class);

    public static final String CREDENTIAL_NAME = "credentialName";
    public static final String CREDENTIAL_TYPE = "credentialType";
    public static final String AUTHORITY_HOST = "authorityHost";
    public static final String TENANT_ID = "tenantId";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String FILETYPE = "filetype";
    public static final String FILENAME = "filename";
    public static final String FILE_PASSWORD = "filePassword";
    public static final String UNAME = "username";
    public static final String PWD = "password";
    public static final String REDIRECT_URL = "redirectUrl";

    public static final String AUTHORITYHOST_PUBLIC = "login.microsoftonline.com";
    public static final String AUTHORITYHOST_GOVENMENT = "login.microsoftonline.us";
    public static final String AUTHORITYHOST_CHINA = "login.partner.microsoftonline.cn";
    public static final String AUTHORITYHOST_GERMANY = "login.microsoftonline.de";
    
    public static final String CREDENTIALTYPE_MANAGED_ID = "Managed identity";
    public static final String CREDENTIALTYPE_CLIENT_SECRET = "Client secret";
    public static final String CREDENTIALTYPE_CLIENT_CERTIFICATE = "Client certificate";
    public static final String CREDENTIALTYPE_USERNAME_PASSWORD = "Username/Password";
    public static final String CREDENTIALTYPE_INTERACTIVE_BROWSER = "Interactive in the browser";

    public static final String FILETYPE_PEM = "PEM";
    public static final String FILETYPE_PFX = "PFX";

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    private transient TokenCredential credential;

    public AzAdCredential() {
        classCount.incrementAndGet();
        trace("AzAdCredential()");
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
        setProperty(new StringProperty(CREDENTIAL_NAME, ""));
        setProperty(new StringProperty(CREDENTIAL_TYPE, CREDENTIALTYPE_CLIENT_SECRET));
        setProperty(new StringProperty(AUTHORITY_HOST, AUTHORITYHOST_PUBLIC));
        setProperty(new StringProperty(TENANT_ID, ""));
        setProperty(new StringProperty(CLIENT_ID, ""));
        setProperty(new StringProperty(CLIENT_SECRET, ""));
        setProperty(new StringProperty(FILETYPE, ""));
        setProperty(new StringProperty(FILENAME, ""));
        setProperty(new StringProperty(FILE_PASSWORD, ""));
        setProperty(new StringProperty(UNAME, ""));
        setProperty(new StringProperty(PWD, ""));
        setProperty(new StringProperty(REDIRECT_URL, ""));
    }

    public void setCredentialName(String credName) {
        setProperty(new StringProperty(CREDENTIAL_NAME, credName));
    }

    public String getCredentialName() {
        return getPropertyAsString(CREDENTIAL_NAME);
    }

    public void setCredentialType(String credentialType) {
        setProperty(new StringProperty(CREDENTIAL_TYPE, credentialType));
    }

    public String getCredentialType() {
        return getPropertyAsString(CREDENTIAL_TYPE);
    }

    public void setAuthorityHost(String authorityHost) {
        setProperty(new StringProperty(AUTHORITY_HOST, authorityHost));
    }

    public String getAuthorityHost() {
        return "https://".concat(getPropertyAsString(AUTHORITY_HOST));
    }

    public void setClientId(String clientId) {
        setProperty(new StringProperty(CLIENT_ID, clientId));
    }

    public String getClientId() {
        return getPropertyAsString(CLIENT_ID);
    }

    public void setTenantId(String tenantId) {
        setProperty(new StringProperty(TENANT_ID, tenantId));
    }

    public String getTenantId() {
        return getPropertyAsString(TENANT_ID);
    }

    public void setClientSecret(String clientSecret) {
        setProperty(new StringProperty(CLIENT_SECRET, clientSecret));
    }

    public String getClientSecret() {
        return getPropertyAsString(CLIENT_SECRET);
    }

    public void setFiletype(String filetype) {
        setProperty(new StringProperty(FILETYPE, filetype));
    }

    public String getFiletype() {
        return getPropertyAsString(FILETYPE);
    }

    public void setFilename(String filename) {
        setProperty(new StringProperty(FILENAME, filename));
    }

    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    public void setFilePassword(String filePassword) {
        setProperty(new StringProperty(FILE_PASSWORD, filePassword));
    }

    public String getFilePassword() {
        return getPropertyAsString(FILE_PASSWORD);
    }

    public void setUsername(String username) {
        setProperty(new StringProperty(UNAME, username));
    }

    public String getUsername() {
        return getPropertyAsString(UNAME);
    }

    public void setPassword(String password) {
        setProperty(new StringProperty(PWD, password));
    }

    public String getPassword() {
        return getPropertyAsString(PWD);
    }

    public void setRedirectUrl(String redirectUrl) {
        setProperty(new StringProperty(PWD, redirectUrl));
    }

    public String getRedirectUrl() {
        return getPropertyAsString(REDIRECT_URL);
    }

    @Override
    public void testEnded() {
        synchronized (this) {
            credential = null;
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
        String credentialName = getCredentialName();
        if (JOrphanUtils.isBlank(credentialName)) {
            log.error("Name for Azure AD credential must not be empty in " + getName());
        } else if (variables.getObject(credentialName) != null) {
            log.error("Azure AD credential already defined for: {}", credentialName);
        } else {
            variables.putObject(credentialName, new AzAdCredentialComponentImpl());
        }
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public Object clone() {
        AzAdCredential aadcred = (AzAdCredential) super.clone();
        synchronized (this) {
            aadcred.credential = credential;
        }
        return aadcred;
    }

    public static AzAdCredentialComponentImpl getCredential(String credentialName) throws Exception {
        Object credObject = JMeterContextService.getContext().getVariables().getObject(credentialName);
        if (credObject == null) {
            throw new Exception("No credential found named: '" + credentialName + "', ensure Variable Name matches Variable Name of Azure AD Credentail.");
        } else {
            if (credObject instanceof AzAdCredentialComponentImpl) {
                AzAdCredentialComponentImpl cred = (AzAdCredentialComponentImpl) credObject;
                return cred;
            } else {
                String errorMsg = "Found object stored under variable:'" + credentialName + "' with class:"
                                + credObject.getClass().getName() + "and value: '" + credObject
                                + " but it's not a AzAdCredentialComponent, check you're not already using this name as another variable";
                log.error(errorMsg);
                throw new Exception(errorMsg);
            }
        }
    }

    public class AzAdCredentialComponentImpl {
        private TokenCredential credential = null;
    
        String requestBody = "";

        private HttpClient httpClientBase() {
            final String proxyHost = JMeterUtils.getPropDefault("https.proxyHost", "").trim();
            final int proxyPort = Integer.parseInt(JMeterUtils.getPropDefault("https.proxyPort", "3128").trim());
            String nonProxyHosts = JMeterUtils.getPropDefault("https.nonProxyHosts", "").trim();
            final String proxyUser = JMeterUtils.getPropDefault("http.proxyUser", "").trim();
            final String proxyPass = JMeterUtils.getPropDefault("http.proxyPass", "");

            nonProxyHosts = nonProxyHosts.concat("|169.254.169.254").replaceFirst("^|", "");

            if (proxyHost.length() > 0) {
                InetSocketAddress address = new InetSocketAddress(proxyHost, proxyPort);
                
                ProxyOptions options = new ProxyOptions(ProxyOptions.Type.HTTP, address).setNonProxyHosts(nonProxyHosts);
                if (proxyUser.length() > 0) {
                    options.setCredentials(proxyUser, proxyPass);
                }
                return (HttpClient) new NettyAsyncHttpClientBuilder().proxy(options).build();
            }
            return HttpClient.createDefault();
        }

        AzAdCredentialComponentImpl() {
            String authorityHost = "";
            String clientId = "";
            String tenantId = "";
            String clientSecret = "";
            String filename = "";
            String filePassword = "";
            String username = "";
            String password = "";

            try {
                switch(getCredentialType()){
                    case CREDENTIALTYPE_MANAGED_ID:
                        clientId = getClientId();
                        requestBody = requestBody.concat("\n")
                            .concat("Client Id: ").concat(clientId);
                        credential = new ManagedIdentityCredentialBuilder()
                            .clientId(clientId)
                            .httpClient(httpClientBase())
                            .build();
                        break;
                    case CREDENTIALTYPE_CLIENT_SECRET:
                        authorityHost = getAuthorityHost();
                        tenantId = getTenantId();
                        clientId = getClientId();
                        clientSecret = getClientSecret();
                        requestBody = requestBody.concat("\n")
                            .concat("Authority host: ").concat(authorityHost).concat("\n")
                            .concat("Tenant Id: ").concat(tenantId).concat("\n")
                            .concat("Client Id: ").concat(clientId).concat("\n")
                            .concat("Client secret: **********");

                        credential = new ClientSecretCredentialBuilder()
                            .authorityHost(authorityHost)
                            .tenantId(tenantId)
                            .clientId(clientId)
                            .clientSecret(clientSecret)
                            .httpClient(httpClientBase())
                            .build();
                        break;
                    case CREDENTIALTYPE_CLIENT_CERTIFICATE:
                        authorityHost = getAuthorityHost();
                        tenantId = getTenantId();
                        clientId = getClientId();
                        filename = getFilename();
                        filePassword = getFilePassword();

                        requestBody = requestBody.concat("\n")
                            .concat("Authority host: ").concat(authorityHost).concat("\n")
                            .concat("Tenant Id: ").concat(tenantId).concat("\n")
                            .concat("Client Id: ").concat(clientId).concat("\n")
                            .concat("Client certificate file: ").concat(filename);
                        ClientCertificateCredentialBuilder spcBuilder = new ClientCertificateCredentialBuilder()
                            .authorityHost(authorityHost)
                            .tenantId(tenantId)
                            .clientId(clientId)
                            .httpClient(httpClientBase());
                        if (getFiletype() == FILETYPE_PFX) {
                            spcBuilder = spcBuilder.pfxCertificate(filename, filePassword);
                        } else {
                            spcBuilder = spcBuilder.pemCertificate(filename);
                        }
                        
                        credential = spcBuilder.build();
                        break;
                    case CREDENTIALTYPE_USERNAME_PASSWORD:
                        authorityHost = getAuthorityHost();
                        tenantId = getTenantId();
                        clientId = getClientId();
                        username = getUsername();
                        password = getPassword();

                        requestBody = requestBody.concat("\n")
                            .concat("Authority host: ").concat(authorityHost).concat("\n")
                            .concat("Tenant Id: ").concat(tenantId).concat("\n")
                            .concat("Client Id: ").concat(clientId).concat("\n")
                            .concat("Username: ").concat(username).concat("\n")
                            .concat("Password: **********");

                        UsernamePasswordCredentialBuilder userpassBuilder = new UsernamePasswordCredentialBuilder()
                            .authorityHost(authorityHost)
                            .tenantId(tenantId)
                            .clientId(clientId)
                            .username(username)
                            .password(password)
                            .httpClient(httpClientBase());
                        
                        credential = userpassBuilder.build();
                        break;
                    case CREDENTIALTYPE_INTERACTIVE_BROWSER:
                        final String redirectUrl = getRedirectUrl();
                        authorityHost = getAuthorityHost();
                        clientId = getClientId();

                        requestBody = requestBody.concat("\n")
                            .concat("Authority host: ").concat(authorityHost).concat("\n")
                            .concat("Client Id: ").concat(clientId).concat("\n")
                            .concat("Redirect URL: ").concat(redirectUrl);

                        InteractiveBrowserCredentialBuilder browserBuilder = new InteractiveBrowserCredentialBuilder()
                            .authorityHost(authorityHost)
                            .clientId(clientId)
                            .httpClient(httpClientBase())
                            .redirectUrl(redirectUrl);

                        credential = browserBuilder.build();
                        break;
                }
                log.debug("Build {} credential: {}", getCredentialType(), requestBody.trim().replace("\n", ", "));
            } catch (Exception ex) {
                log.error("Build credential error: {}", ex.getMessage(), ex);
            }
        }

        AzAdCredentialComponentImpl(TokenCredential cred) {
            credential = cred;
        }

        public TokenCredential getCredential() throws Exception {
            return credential;

        }

        public String getRequestBody() {
            return requestBody;
        }
    }

    /*
    @Override
    public void addConfigElement(ConfigTestElement config) {
    }
    */

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
