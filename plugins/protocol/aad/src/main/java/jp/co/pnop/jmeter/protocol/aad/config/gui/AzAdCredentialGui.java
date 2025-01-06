package jp.co.pnop.jmeter.protocol.aad.config.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.aad.config.AzAdCredential;


public class AzAdCredentialGui extends AbstractConfigGui implements ChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzAdCredentialGui.class);

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    private String[] AUTHORITY_HOST_LABELS = {
        AzAdCredential.AUTHORITYHOST_PUBLIC,
        AzAdCredential.AUTHORITYHOST_GOVENMENT,
        AzAdCredential.AUTHORITYHOST_CHINA,
        AzAdCredential.AUTHORITYHOST_GERMANY
    };

    private String[] CREDENTIAL_TYPE_LABELS = {
        AzAdCredential.CREDENTIALTYPE_CLIENT_CERTIFICATE,
        AzAdCredential.CREDENTIALTYPE_CLIENT_SECRET,
        AzAdCredential.CREDENTIALTYPE_MANAGED_ID,
        AzAdCredential.CREDENTIALTYPE_WORKLOAD_IDENTITY,
        AzAdCredential.CREDENTIALTYPE_AZURE_CLI,
        AzAdCredential.CREDENTIALTYPE_AZURE_DEVELOPER_CLI,
        AzAdCredential.CREDENTIALTYPE_AZURE_POWERSHELL,
        //AzAdCredential.CREDENTIALTYPE_VISUAL_STUDIO_CODE,
        //AzAdCredential.CREDENTIALTYPE_INTELLIJ,
        AzAdCredential.CREDENTIALTYPE_DEFAULT_AZURE_CREDENTIAL,
        //AzAdCredential.CREDENTIALTYPE_USERNAME_PASSWORD,
        //AzAdCredential.CREDENTIALTYPE_INTERACTIVE_BROWSER
    };
    private JLabeledTextField credentialName;
    private JLabel credentialTypeLabel;
    private JLabeledChoice credentialType;
    private JPanel credentialPanel;
    private JLabeledTextField managedIdClientId;
    private JLabeledChoice clientSecretAuthorityHost;
    private JLabeledTextField clientSecretTenantId;
    private JLabeledTextField clientSecretClientId;
    private JLabeledPasswordField clientSecretClientSecret;
    private JLabeledChoice clientCertificateAuthorityHost;
    private JLabeledTextField clientCertificateTenantId;
    private JLabeledTextField clientCertificateClientId;
    private JRadioButton clientCertificateFiletypePEM;
    private JRadioButton clientCertificateFiletypePFX;
    private JLabeledTextField clientCertificateFilename;
    private JLabeledPasswordField clientCertificateFilePassword;
    private JLabeledTextField workloadIdentityAuthorityHost;
    private JLabeledTextField workloadIdentityTenantId;
    private JLabeledTextField workloadIdentityAdditinalyAllowedTenants;
    private JLabeledTextField workloadIdentityClientId;
    private JLabeledTextField workloadIdentityTokenFilePath;
    private JLabeledTextField azureCliTenantId;
    private JLabeledTextField azureCliAdditionallyAllowedTenants;
    private JLabeledTextField azureDeveloperCliTenantId;
    private JLabeledTextField azureDeveloperCliAdditionallyAllowedTenants;
    private JLabeledTextField azurePowerShellTenantId;
    private JLabeledTextField azurePowerShellAdditionallyAllowedTenants;
    private JLabeledTextField visualStudioCodeTenantId;
    private JLabeledTextField visualStudioCodeAdditionallyAllowedTenants;
    private JLabeledTextField intelliJTenantId;
    private JLabeledTextField intelliJAdditionallyAllowedTenants;
    private JLabeledTextField intelliJIntelliJKeePassDatabasePath;
    private JLabeledChoice defaultAzureCredentialAuthorityHost;
    private JLabeledTextField defaultAzureCredentialTenantId;
    private JLabeledTextField defaultAzureCredentialAdditionallyAllowedTenants;
    private JLabeledTextField defaultAzureCredentialManagedIdentityClientId;
    private JLabeledTextField defaultAzureCredentialWorkloadIdentityClientId;
    private JLabeledTextField defaultAzureCredentialIntelliJKeePassDatabasePath;
    private JLabeledChoice usernamepasswordAuthorityHost;
    private JLabeledTextField usernamepasswordTenantId;
    private JLabeledTextField usernamepasswordClientId;
    private JLabeledTextField usernamepasswordUsername;
    private JLabeledPasswordField usernamepasswordPassword;
    private JLabeledChoice interactiveBrowserAuthorityHost;
    private JLabeledTextField interactiveBrowserTenantId;
    private JLabeledTextField interactiveBrowserClientId;
    private JLabeledTextField interactiveBrowserRedirectUrl;
    
    private ButtonGroup clientCertificateFiletypeGroup = new ButtonGroup();

    public AzAdCredentialGui() {
        classCount.incrementAndGet();
        trace("AzAdCredentialGui()");
        init();
    }

    /**
     * @return a string for the sampleResultGui Title
     */
    private String getTitle() {
        return this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        credentialName.setText(element.getPropertyAsString(AzAdCredential.CREDENTIAL_NAME));
        credentialType.setText(element.getPropertyAsString(AzAdCredential.CREDENTIAL_TYPE));
        toggleCredentialTypeValue();
        switch (credentialType.getText()) {
            case AzAdCredential.CREDENTIALTYPE_MANAGED_ID:
                managedIdClientId.setText(element.getPropertyAsString(AzAdCredential.CLIENT_ID));
                break;
            case AzAdCredential.CREDENTIALTYPE_CLIENT_SECRET:
                clientSecretAuthorityHost.setText(element.getPropertyAsString(AzAdCredential.AUTHORITY_HOST));
                clientSecretTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                clientSecretClientId.setText(element.getPropertyAsString(AzAdCredential.CLIENT_ID));
                clientSecretClientSecret.setText(element.getPropertyAsString(AzAdCredential.CLIENT_SECRET));
                break;
            case AzAdCredential.CREDENTIALTYPE_CLIENT_CERTIFICATE:
                clientCertificateAuthorityHost.setText(element.getPropertyAsString(AzAdCredential.AUTHORITY_HOST));
                clientCertificateTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                clientCertificateClientId.setText(element.getPropertyAsString(AzAdCredential.CLIENT_ID));
                final String fileType = element.getPropertyAsString(AzAdCredential.FILETYPE);
                if (fileType.equals(AzAdCredential.FILETYPE_PFX)) {
                    clientCertificateFiletypePFX.setSelected(true);
                } else {
                    clientCertificateFiletypePEM.setSelected(true);
                }
                clientCertificateFilename.setText(element.getPropertyAsString(AzAdCredential.FILENAME));
                clientCertificateFilePassword.setText(element.getPropertyAsString(AzAdCredential.FILE_PASSWORD));
                toggleClientCertificateFilePasswordValue();
                break;

            case AzAdCredential.CREDENTIALTYPE_WORKLOAD_IDENTITY:
                workloadIdentityAuthorityHost.setText(element.getPropertyAsString(AzAdCredential.AUTHORITY_HOST));
                workloadIdentityTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                workloadIdentityAdditinalyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                workloadIdentityClientId.setText(element.getPropertyAsString(AzAdCredential.CLIENT_ID));
                workloadIdentityTokenFilePath.setText(element.getPropertyAsString(AzAdCredential.TOKEN_FILE_PATH));
                break;
            case AzAdCredential.CREDENTIALTYPE_AZURE_CLI:
                azureCliTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                azureCliAdditionallyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                break;
            case AzAdCredential.CREDENTIALTYPE_AZURE_DEVELOPER_CLI:
                azureDeveloperCliTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                azureDeveloperCliAdditionallyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                break;
            case AzAdCredential.CREDENTIALTYPE_AZURE_POWERSHELL:
                azurePowerShellTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                azurePowerShellAdditionallyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                break;
            case AzAdCredential.CREDENTIALTYPE_VISUAL_STUDIO_CODE:
                visualStudioCodeTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                visualStudioCodeAdditionallyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                break;
            case AzAdCredential.CREDENTIALTYPE_INTELLIJ:
                intelliJTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                intelliJAdditionallyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                intelliJIntelliJKeePassDatabasePath.setText(element.getPropertyAsString(AzAdCredential.INTELLIJ_KEEPASS_DATABASE_PATH));
                break;
            case AzAdCredential.CREDENTIALTYPE_DEFAULT_AZURE_CREDENTIAL:
                defaultAzureCredentialAuthorityHost.setText(element.getPropertyAsString(AzAdCredential.AUTHORITY_HOST));
                defaultAzureCredentialTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                defaultAzureCredentialAdditionallyAllowedTenants.setText(element.getPropertyAsString(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS));
                defaultAzureCredentialManagedIdentityClientId.setText(element.getPropertyAsString(AzAdCredential.MANAGED_IDENTITY_CLIENT_ID));
                defaultAzureCredentialWorkloadIdentityClientId.setText(element.getPropertyAsString(AzAdCredential.WORKLOAD_IDENTITY_CLIENT_ID));
                defaultAzureCredentialIntelliJKeePassDatabasePath.setText(element.getPropertyAsString(AzAdCredential.INTELLIJ_KEEPASS_DATABASE_PATH));
                break;
            case AzAdCredential.CREDENTIALTYPE_USERNAME_PASSWORD:
                usernamepasswordAuthorityHost.setText(element.getPropertyAsString(AzAdCredential.AUTHORITY_HOST));
                usernamepasswordTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                usernamepasswordClientId.setText(element.getPropertyAsString(AzAdCredential.CLIENT_ID));
                usernamepasswordUsername.setText(element.getPropertyAsString(AzAdCredential.UNAME));
                usernamepasswordPassword.setText(element.getPropertyAsString(AzAdCredential.PWD));
                break;
            case AzAdCredential.CREDENTIALTYPE_INTERACTIVE_BROWSER:
                interactiveBrowserAuthorityHost.setText(element.getPropertyAsString(AzAdCredential.AUTHORITY_HOST));
                interactiveBrowserTenantId.setText(element.getPropertyAsString(AzAdCredential.TENANT_ID));
                interactiveBrowserClientId.setText(element.getPropertyAsString(AzAdCredential.CLIENT_ID));
                interactiveBrowserRedirectUrl.setText(element.getPropertyAsString(AzAdCredential.REDIRECT_URL));
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "Microsoft Entra ID Credential";
    }

    @Override
    public void modifyTestElement(TestElement element) {
        element.clear();
        super.configureTestElement(element);
        element.setProperty(AzAdCredential.CREDENTIAL_NAME, credentialName.getText());
        element.setProperty(AzAdCredential.CREDENTIAL_TYPE, credentialType.getText());
        switch (credentialType.getText()) {
            case AzAdCredential.CREDENTIALTYPE_MANAGED_ID:
                element.setProperty(AzAdCredential.CLIENT_ID, managedIdClientId.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_CLIENT_SECRET:
                element.setProperty(AzAdCredential.AUTHORITY_HOST, clientSecretAuthorityHost.getText());
                element.setProperty(AzAdCredential.TENANT_ID, clientSecretTenantId.getText());
                element.setProperty(AzAdCredential.CLIENT_ID, clientSecretClientId.getText());
                element.setProperty(AzAdCredential.CLIENT_SECRET, clientSecretClientSecret.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_CLIENT_CERTIFICATE:
                element.setProperty(AzAdCredential.AUTHORITY_HOST, clientCertificateAuthorityHost.getText());
                element.setProperty(AzAdCredential.TENANT_ID, clientCertificateTenantId.getText());
                element.setProperty(AzAdCredential.CLIENT_ID, clientCertificateClientId.getText());
                if (clientCertificateFiletypePFX.isSelected()) {
                    element.setProperty(new StringProperty(AzAdCredential.FILETYPE, AzAdCredential.FILETYPE_PFX));
                } else { // PEM
                    element.setProperty(new StringProperty(AzAdCredential.FILETYPE, AzAdCredential.FILETYPE_PEM));
                }
                element.setProperty(AzAdCredential.FILENAME, clientCertificateFilename.getText());
                element.setProperty(AzAdCredential.FILE_PASSWORD, clientCertificateFilePassword.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_WORKLOAD_IDENTITY:
                element.setProperty(AzAdCredential.AUTHORITY_HOST, workloadIdentityAuthorityHost.getText());
                element.setProperty(AzAdCredential.TENANT_ID, workloadIdentityTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, workloadIdentityAdditinalyAllowedTenants.getText());
                element.setProperty(AzAdCredential.CLIENT_ID, workloadIdentityClientId.getText());
                element.setProperty(AzAdCredential.TOKEN_FILE_PATH, workloadIdentityTokenFilePath.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_AZURE_CLI:
                element.setProperty(AzAdCredential.TENANT_ID, azureCliTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, azureCliAdditionallyAllowedTenants.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_AZURE_DEVELOPER_CLI:
                element.setProperty(AzAdCredential.TENANT_ID, azureDeveloperCliTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, azureDeveloperCliAdditionallyAllowedTenants.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_AZURE_POWERSHELL:
                element.setProperty(AzAdCredential.TENANT_ID, azurePowerShellTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, azurePowerShellAdditionallyAllowedTenants.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_VISUAL_STUDIO_CODE:
                element.setProperty(AzAdCredential.TENANT_ID, visualStudioCodeTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, visualStudioCodeAdditionallyAllowedTenants.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_INTELLIJ:
                element.setProperty(AzAdCredential.TENANT_ID, intelliJTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, intelliJAdditionallyAllowedTenants.getText());
                element.setProperty(AzAdCredential.INTELLIJ_KEEPASS_DATABASE_PATH, intelliJIntelliJKeePassDatabasePath.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_DEFAULT_AZURE_CREDENTIAL:
                element.setProperty(AzAdCredential.AUTHORITY_HOST, defaultAzureCredentialAuthorityHost.getText());
                element.setProperty(AzAdCredential.TENANT_ID, defaultAzureCredentialTenantId.getText());
                element.setProperty(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS, defaultAzureCredentialAdditionallyAllowedTenants.getText());
                element.setProperty(AzAdCredential.MANAGED_IDENTITY_CLIENT_ID, defaultAzureCredentialManagedIdentityClientId.getText());
                element.setProperty(AzAdCredential.WORKLOAD_IDENTITY_CLIENT_ID, defaultAzureCredentialWorkloadIdentityClientId.getText());
                element.setProperty(AzAdCredential.INTELLIJ_KEEPASS_DATABASE_PATH, defaultAzureCredentialIntelliJKeePassDatabasePath.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_USERNAME_PASSWORD:
                element.setProperty(AzAdCredential.AUTHORITY_HOST, usernamepasswordAuthorityHost.getText());
                element.setProperty(AzAdCredential.TENANT_ID, usernamepasswordTenantId.getText());
                element.setProperty(AzAdCredential.CLIENT_ID, usernamepasswordClientId.getText());
                element.setProperty(AzAdCredential.UNAME, usernamepasswordUsername.getText());
                element.setProperty(AzAdCredential.PWD, usernamepasswordPassword.getText());
                break;
            case AzAdCredential.CREDENTIALTYPE_INTERACTIVE_BROWSER:
                element.setProperty(AzAdCredential.AUTHORITY_HOST, interactiveBrowserAuthorityHost.getText());
                element.setProperty(AzAdCredential.TENANT_ID, interactiveBrowserTenantId.getText());
                element.setProperty(AzAdCredential.CLIENT_ID, interactiveBrowserClientId.getText());
                element.setProperty(AzAdCredential.REDIRECT_URL, interactiveBrowserRedirectUrl.getText());
                break;                
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();

        credentialName.setText("");
        credentialType.setText(AzAdCredential.CREDENTIALTYPE_CLIENT_CERTIFICATE);
        managedIdClientId.setText("");
        clientSecretAuthorityHost.setText(AzAdCredential.AUTHORITYHOST_PUBLIC);
        clientSecretTenantId.setText("");
        clientSecretClientId.setText("");
        clientSecretClientSecret.setText("");
        clientCertificateAuthorityHost.setText(AzAdCredential.AUTHORITYHOST_PUBLIC);
        clientCertificateTenantId.setText("");
        clientCertificateClientId.setText("");
        clientCertificateFiletypePEM.setSelected(true);
        clientCertificateFiletypePFX.setSelected(false);
        clientCertificateFilename.setText("");
        clientCertificateFilePassword.setText("");
        workloadIdentityAuthorityHost.setText(AzAdCredential.AUTHORITYHOST_PUBLIC);
        workloadIdentityTenantId.setText("");
        workloadIdentityAdditinalyAllowedTenants.setText("");
        workloadIdentityClientId.setText("");
        workloadIdentityTokenFilePath.setText("");
        azureCliTenantId.setText("");
        azureCliAdditionallyAllowedTenants.setText("");
        azureDeveloperCliTenantId.setText("");
        azureDeveloperCliAdditionallyAllowedTenants.setText("");
        azurePowerShellTenantId.setText("");
        azurePowerShellAdditionallyAllowedTenants.setText("");
        visualStudioCodeTenantId.setText("");
        visualStudioCodeAdditionallyAllowedTenants.setText("");
        intelliJTenantId.setText("");
        intelliJAdditionallyAllowedTenants.setText("");
        intelliJIntelliJKeePassDatabasePath.setText("");
        defaultAzureCredentialAuthorityHost.setText(AzAdCredential.AUTHORITYHOST_PUBLIC);
        defaultAzureCredentialTenantId.setText("");
        defaultAzureCredentialAdditionallyAllowedTenants.setText("");
        defaultAzureCredentialManagedIdentityClientId.setText("");
        defaultAzureCredentialWorkloadIdentityClientId.setText("");
        defaultAzureCredentialIntelliJKeePassDatabasePath.setText("");
        usernamepasswordAuthorityHost.setText(AzAdCredential.AUTHORITYHOST_PUBLIC);
        usernamepasswordTenantId.setText("");
        usernamepasswordClientId.setText("");
        usernamepasswordUsername.setText("");
        usernamepasswordPassword.setText("");
        interactiveBrowserAuthorityHost.setText(AzAdCredential.AUTHORITYHOST_PUBLIC);
        interactiveBrowserTenantId.setText("");
        interactiveBrowserClientId.setText("");
        interactiveBrowserRedirectUrl.setText("");
    }

    @Override
    public TestElement createTestElement() {
        AzAdCredential credential = new AzAdCredential();
        modifyTestElement(credential);
        return credential;
    }

    private JPanel createCredentialNamePanel() {
        credentialName = new JLabeledTextField("Variable Name for created credential:");
        credentialName.setName(AzAdCredential.CREDENTIAL_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Variable Name Bound to Credential"));
        panel.add(credentialName);
        
        return panel;
    }

    private JPanel createCredentialTypePanel() {
        credentialTypeLabel = new JLabel("Credential type:");

        credentialType = new JLabeledChoice("", CREDENTIAL_TYPE_LABELS);
        credentialType.setName(AzAdCredential.CREDENTIAL_TYPE);
        credentialType.addChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Credential configuration"));
        panel.add(credentialTypeLabel, BorderLayout.WEST);
        panel.add(credentialType, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCredentialPanel() {
        credentialPanel = new JPanel(new CardLayout());
        credentialPanel.add(createManagedIdPanel(), AzAdCredential.CREDENTIALTYPE_MANAGED_ID);
        credentialPanel.add(createClientSecretPanel(), AzAdCredential.CREDENTIALTYPE_CLIENT_SECRET);
        credentialPanel.add(createClientCertificatePanel(), AzAdCredential.CREDENTIALTYPE_CLIENT_CERTIFICATE);
        credentialPanel.add(createWorkloadIdentityPanel(), AzAdCredential.CREDENTIALTYPE_WORKLOAD_IDENTITY);
        credentialPanel.add(createAzureCliPanel(), AzAdCredential.CREDENTIALTYPE_AZURE_CLI);
        credentialPanel.add(createAzureDeveloperCliPanel(), AzAdCredential.CREDENTIALTYPE_AZURE_DEVELOPER_CLI);
        credentialPanel.add(createAzurePowerShellPanel(), AzAdCredential.CREDENTIALTYPE_AZURE_POWERSHELL);
        credentialPanel.add(createVisualStudioCodePanel(), AzAdCredential.CREDENTIALTYPE_VISUAL_STUDIO_CODE);
        credentialPanel.add(createIntelliJPanel(), AzAdCredential.CREDENTIALTYPE_INTELLIJ);
        credentialPanel.add(createDefaultAzureCredentialPanel(), AzAdCredential.CREDENTIALTYPE_DEFAULT_AZURE_CREDENTIAL);
        credentialPanel.add(createUsernamePasswordPanel(), AzAdCredential.CREDENTIALTYPE_USERNAME_PASSWORD);
        credentialPanel.add(createInteractiveBrowserPanel(), AzAdCredential.CREDENTIALTYPE_INTERACTIVE_BROWSER);

        return credentialPanel;
    }

    private JPanel createManagedIdPanel() {
        managedIdClientId = new JLabeledTextField("Client Id:");
        managedIdClientId.setName(AzAdCredential.CLIENT_ID);

        JPanel panel = new VerticalPanel();
        panel.add(managedIdClientId);

        return panel;
    }

    private JPanel createClientSecretPanel() {
        JPanel authorityHostPanel = new HorizontalPanel();
        JLabel authorityHostLabel = new JLabel("Authority host:");
        clientSecretAuthorityHost = new JLabeledChoice("", AUTHORITY_HOST_LABELS, true, false);
        clientSecretAuthorityHost.setName(AzAdCredential.AUTHORITY_HOST);
        authorityHostPanel.add(authorityHostLabel);
        authorityHostPanel.add(clientSecretAuthorityHost);

        clientSecretTenantId = new JLabeledTextField("Tenant Id:");
        clientSecretTenantId.setName(AzAdCredential.TENANT_ID);
        clientSecretClientId = new JLabeledTextField("Client Id:");
        clientSecretClientId.setName(AzAdCredential.CLIENT_ID);
        clientSecretClientSecret = new JLabeledPasswordField("Client Secret:");
        clientSecretClientSecret.setName(AzAdCredential.CLIENT_SECRET);
        
        JPanel panel = new VerticalPanel();
        panel.add(authorityHostPanel);
        panel.add(clientSecretTenantId);
        panel.add(clientSecretClientId);
        panel.add(clientSecretClientSecret);

        return panel;
    }

    private JPanel createClientCertificatePanel() {
        JPanel authorityHostPanel = new HorizontalPanel();
        JLabel authorityHostLabel = new JLabel("Authority host:");
        clientCertificateAuthorityHost = new JLabeledChoice("", AUTHORITY_HOST_LABELS, true, false);
        clientCertificateAuthorityHost.setName(AzAdCredential.AUTHORITY_HOST);
        authorityHostPanel.add(authorityHostLabel);
        authorityHostPanel.add(clientCertificateAuthorityHost);

        clientCertificateTenantId = new JLabeledTextField("Tenant Id:");
        clientCertificateTenantId.setName(AzAdCredential.TENANT_ID);
        clientCertificateClientId = new JLabeledTextField("Client Id:");
        clientCertificateClientId.setName(AzAdCredential.CLIENT_ID);
        JLabel clientCertificateFiletypeLabel = new JLabel("File type:");
        clientCertificateFiletypePEM = new JRadioButton("PEM");
        clientCertificateFiletypePFX = new JRadioButton("PFX");
        clientCertificateFilename = new JLabeledTextField("Filename:");
        clientCertificateFilename.setName(AzAdCredential.FILENAME);
        clientCertificateFilePassword = new JLabeledPasswordField("Password:");
        clientCertificateFilePassword.setName(AzAdCredential.FILE_PASSWORD);

        JPanel filetypePanel = new HorizontalPanel();
        filetypePanel.add(clientCertificateFiletypeLabel);
        filetypePanel.add(clientCertificateFiletypePEM);
        filetypePanel.add(clientCertificateFiletypePFX);
        clientCertificateFiletypeGroup.add(clientCertificateFiletypePEM);
        clientCertificateFiletypeGroup.add(clientCertificateFiletypePFX);
        clientCertificateFiletypePFX.addChangeListener(this);

        JPanel panel = new VerticalPanel();
        panel.add(authorityHostPanel);
        panel.add(clientCertificateTenantId);
        panel.add(clientCertificateClientId);
        panel.add(filetypePanel);
        panel.add(clientCertificateFilename);
        panel.add(clientCertificateFilePassword);
        return panel;
    }

    private JPanel createWorkloadIdentityPanel() {
        workloadIdentityAuthorityHost = new JLabeledTextField("Authority host:");
        workloadIdentityTenantId = new JLabeledTextField("Tenant Id:");
        workloadIdentityTenantId.setName(AzAdCredential.TENANT_ID);
        workloadIdentityAdditinalyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        workloadIdentityAdditinalyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);
        workloadIdentityClientId = new JLabeledTextField("Client Id:");
        workloadIdentityClientId.setName(AzAdCredential.CLIENT_ID);
        workloadIdentityTokenFilePath = new JLabeledTextField("Token file path:");
        workloadIdentityTokenFilePath.setName(AzAdCredential.TOKEN_FILE_PATH);

        JPanel panel = new VerticalPanel();
        panel.add(workloadIdentityAuthorityHost);
        panel.add(workloadIdentityTenantId);
        panel.add(workloadIdentityAdditinalyAllowedTenants);
        panel.add(workloadIdentityClientId);
        panel.add(workloadIdentityTokenFilePath);

        return panel;
    }

    private JPanel createAzureCliPanel() {
        azureCliTenantId = new JLabeledTextField("Tenant Id:");
        azureCliTenantId.setName(AzAdCredential.TENANT_ID);
        azureCliAdditionallyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        azureCliAdditionallyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);

        JPanel panel = new VerticalPanel();
        panel.add(azureCliTenantId);
        panel.add(azureCliAdditionallyAllowedTenants);

        return panel;
    }

    private JPanel createAzureDeveloperCliPanel() {
        azureDeveloperCliTenantId = new JLabeledTextField("Tenant Id:");
        azureDeveloperCliTenantId.setName(AzAdCredential.TENANT_ID);
        azureDeveloperCliAdditionallyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        azureDeveloperCliAdditionallyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);

        JPanel panel = new VerticalPanel();
        panel.add(azureDeveloperCliTenantId);
        panel.add(azureDeveloperCliAdditionallyAllowedTenants);

        return panel;
    }

    private JPanel createAzurePowerShellPanel() {
        azurePowerShellTenantId = new JLabeledTextField("Tenant Id:");
        azurePowerShellTenantId.setName(AzAdCredential.TENANT_ID);
        azurePowerShellAdditionallyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        azurePowerShellAdditionallyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);

        JPanel panel = new VerticalPanel();
        panel.add(azurePowerShellTenantId);
        panel.add(azurePowerShellAdditionallyAllowedTenants);

        return panel;
    }

    private JPanel createVisualStudioCodePanel() {
        visualStudioCodeTenantId = new JLabeledTextField("Tenant Id:");
        visualStudioCodeTenantId.setName(AzAdCredential.TENANT_ID);
        visualStudioCodeAdditionallyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        visualStudioCodeAdditionallyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);

        JPanel panel = new VerticalPanel();
        panel.add(visualStudioCodeTenantId);
        panel.add(visualStudioCodeAdditionallyAllowedTenants);

        return panel;
    }

    private JPanel createIntelliJPanel() {
        intelliJTenantId = new JLabeledTextField("Tenant Id:");
        intelliJTenantId.setName(AzAdCredential.TENANT_ID);
        intelliJAdditionallyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        intelliJAdditionallyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);
        intelliJIntelliJKeePassDatabasePath = new JLabeledTextField("KeePass database path:");
        intelliJIntelliJKeePassDatabasePath.setName(AzAdCredential.INTELLIJ_KEEPASS_DATABASE_PATH);

        JPanel panel = new VerticalPanel();
        panel.add(intelliJTenantId);
        panel.add(intelliJAdditionallyAllowedTenants);
        panel.add(intelliJIntelliJKeePassDatabasePath);

        return panel;
    }

    private JPanel createDefaultAzureCredentialPanel() {
        JPanel authorityHostPanel = new HorizontalPanel();
        JLabel authorityHostLabel = new JLabel("Authority host:");
        defaultAzureCredentialAuthorityHost = new JLabeledChoice("", AUTHORITY_HOST_LABELS, true, false);
        defaultAzureCredentialAuthorityHost.setName(AzAdCredential.AUTHORITY_HOST);
        authorityHostPanel.add(authorityHostLabel);
        authorityHostPanel.add(defaultAzureCredentialAuthorityHost);

        defaultAzureCredentialTenantId = new JLabeledTextField("Tenant Id:");
        defaultAzureCredentialTenantId.setName(AzAdCredential.TENANT_ID);
        defaultAzureCredentialAdditionallyAllowedTenants = new JLabeledTextField("Additionally allowed tenants:");
        defaultAzureCredentialAdditionallyAllowedTenants.setName(AzAdCredential.ADDITIONALLY_ALLOWED_TENANTS);
        defaultAzureCredentialManagedIdentityClientId = new JLabeledTextField("Managed identity Client ID:");
        defaultAzureCredentialManagedIdentityClientId.setName(AzAdCredential.CLIENT_ID);
        defaultAzureCredentialWorkloadIdentityClientId = new JLabeledTextField("Workload Identity Client ID:");
        defaultAzureCredentialWorkloadIdentityClientId.setName(AzAdCredential.WORKLOAD_IDENTITY_CLIENT_ID);
        defaultAzureCredentialIntelliJKeePassDatabasePath = new JLabeledTextField("IntelliJ KeePass database path:");
        defaultAzureCredentialIntelliJKeePassDatabasePath.setName(AzAdCredential.INTELLIJ_KEEPASS_DATABASE_PATH);

        JPanel panel = new VerticalPanel();
        panel.add(authorityHostPanel);
        panel.add(defaultAzureCredentialTenantId);
        panel.add(defaultAzureCredentialManagedIdentityClientId);
        panel.add(defaultAzureCredentialWorkloadIdentityClientId);
        panel.add(defaultAzureCredentialIntelliJKeePassDatabasePath);

        return panel;
    }

    private JPanel createUsernamePasswordPanel() {
        JPanel authorityHostPanel = new HorizontalPanel();
        JLabel authorityHostLabel = new JLabel("Authority host:");
        usernamepasswordAuthorityHost = new JLabeledChoice("", AUTHORITY_HOST_LABELS, true, false);
        usernamepasswordAuthorityHost.setName(AzAdCredential.AUTHORITY_HOST);
        authorityHostPanel.add(authorityHostLabel);
        authorityHostPanel.add(usernamepasswordAuthorityHost);

        usernamepasswordTenantId = new JLabeledTextField("Tenant Id:");
        usernamepasswordTenantId.setName(AzAdCredential.TENANT_ID);
        usernamepasswordClientId = new JLabeledTextField("Client Id:");
        usernamepasswordClientId.setName(AzAdCredential.CLIENT_ID);
        usernamepasswordUsername = new JLabeledTextField("Username:");
        usernamepasswordUsername.setName(AzAdCredential.UNAME);
        usernamepasswordPassword = new JLabeledPasswordField("Password:");
        usernamepasswordPassword.setName(AzAdCredential.PWD);
        
        JPanel panel = new VerticalPanel();
        panel.add(authorityHostPanel);
        panel.add(usernamepasswordTenantId);
        panel.add(usernamepasswordClientId);
        panel.add(usernamepasswordUsername);
        panel.add(usernamepasswordPassword);

        return panel;
    }

    private JPanel createInteractiveBrowserPanel() {
        JPanel authorityHostPanel = new HorizontalPanel();
        JLabel authorityHostLabel = new JLabel("Authority host:");
        interactiveBrowserAuthorityHost = new JLabeledChoice("", AUTHORITY_HOST_LABELS, true, false);
        interactiveBrowserAuthorityHost.setName(AzAdCredential.AUTHORITY_HOST);
        authorityHostPanel.add(authorityHostLabel);
        authorityHostPanel.add(interactiveBrowserAuthorityHost);

        interactiveBrowserTenantId = new JLabeledTextField("Tenant Id:");
        interactiveBrowserTenantId.setName(AzAdCredential.TENANT_ID);
        interactiveBrowserClientId = new JLabeledTextField("Client Id:");
        interactiveBrowserClientId.setName(AzAdCredential.CLIENT_ID);
        interactiveBrowserRedirectUrl = new JLabeledTextField("Redirect Url:");
        interactiveBrowserRedirectUrl.setName(AzAdCredential.REDIRECT_URL);

        JPanel panel = new VerticalPanel();
        panel.add(authorityHostPanel);
        panel.add(interactiveBrowserTenantId);
        panel.add(interactiveBrowserClientId);
        panel.add(interactiveBrowserRedirectUrl);

        return panel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createCredentialNamePanel());
        mainPanel.add(createCredentialTypePanel());
        mainPanel.add(createCredentialPanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource().equals(credentialType)) {
            toggleCredentialTypeValue();
        } else if (event.getSource().equals(clientCertificateFiletypePFX)) {
            toggleClientCertificateFilePasswordValue();
        }
    }

    /**
     * Visualize selected credential type.
     */
    private void toggleCredentialTypeValue() {
        CardLayout credentialTypeLayout = (CardLayout) credentialPanel.getLayout();
        credentialTypeLayout.show(credentialPanel, credentialType.getText());
    }

    private void toggleClientCertificateFilePasswordValue() {
        clientCertificateFilePassword.setEnabled(clientCertificateFiletypePFX.isSelected());
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
