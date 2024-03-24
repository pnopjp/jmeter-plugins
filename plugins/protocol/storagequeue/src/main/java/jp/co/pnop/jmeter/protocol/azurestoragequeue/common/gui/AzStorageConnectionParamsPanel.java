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

 package jp.co.pnop.jmeter.protocol.azurestoragequeue.common.gui;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledPasswordField;
import org.apache.jorphan.gui.JLabeledTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azurestoragequeue.common.AzStorageConnectionParams;

public class AzStorageConnectionParamsPanel extends AbstractConfigGui implements ChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzStorageConnectionParamsPanel.class);

    private JLabeledChoice authType;
    private String[] AUTH_TYPE_LABELS = {
        AzStorageConnectionParams.AUTHTYPE_CONNECTION_STRING,
        AzStorageConnectionParams.AUTHTYPE_KEY,
        AzStorageConnectionParams.AUTHTYPE_SAS,
        AzStorageConnectionParams.AUTHTYPE_ENTRAID
    };
    private JLabeledTextField connectionString;
    private JLabeledTextField aadCredential;
    private JLabeledChoice defaultEndpointsProtocol;
    private String[] PROTOCOL_LABELS = {
        AzStorageConnectionParams.PROTOCOL_HTTP,
        AzStorageConnectionParams.PROTOCOL_HTTPS
    };
    private JLabeledTextField accountName;
    private JLabeledPasswordField storageKey;
    private JLabeledTextField endpointSuffix;
    private JLabeledTextField endpointUrl;
    private JLabeledTextField queueName;
    private JLabeledPasswordField sasToken;

    private JPanel authTypePanel = new JPanel(new BorderLayout(5, 0));
    private VerticalPanel connectionStringPanel = new VerticalPanel();
    private VerticalPanel aadCredentialPanel = new VerticalPanel();
    private VerticalPanel storageKeyPanel = new VerticalPanel();
    private VerticalPanel endpointUrlPanel = new VerticalPanel();
    private VerticalPanel queueNamePanel = new VerticalPanel();
    private VerticalPanel sasTokenPanel = new VerticalPanel();

    private static final int verticalStrut = 5;
    
    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzStorageConnectionParamsPanel() {
        init();
        classCount.incrementAndGet();
        trace("AzStorageConnectionParamsPanel()");
    }
    
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        AzStorageConnectionParams connection = new AzStorageConnectionParams();
        modifyTestElement(connection);
        return (TestElement) connection.clone();
    }

    @Override
    public void modifyTestElement(TestElement element) {
        element.clear();
        super.configureTestElement(element);

        String auth = authType.getText();
        element.setProperty(AzStorageConnectionParams.AUTH_TYPE, auth);
        
        if (auth.equals(AzStorageConnectionParams.AUTHTYPE_KEY)) {
            updateConnectionString();
            element.setProperty(AzStorageConnectionParams.CONNECTION_STRING, connectionString.getText());
            element.setProperty(AzStorageConnectionParams.DEFAULT_ENDPOINTS_PROTOCOL, defaultEndpointsProtocol.getText());
            element.setProperty(AzStorageConnectionParams.ACCOUNT_NAME, accountName.getText());
            element.setProperty(AzStorageConnectionParams.STORAGE_KEY, storageKey.getText());
            element.setProperty(AzStorageConnectionParams.ENDPOINT_SUFFIX, endpointSuffix.getText());
            element.setProperty(AzStorageConnectionParams.QUEUE_NAME, queueName.getText());
        } else if (auth.equals(AzStorageConnectionParams.AUTHTYPE_SAS)) {
            element.setProperty(AzStorageConnectionParams.ENDPOINT_URL, endpointUrl.getText());
            element.setProperty(AzStorageConnectionParams.QUEUE_NAME, queueName.getText());
            element.setProperty(AzStorageConnectionParams.SAS_TOKEN, sasToken.getText());
        } else if (auth.equals(AzStorageConnectionParams.AUTHTYPE_ENTRAID) || auth.equals(AzStorageConnectionParams.AUTHTYPE_AAD)) {
            element.setProperty(AzStorageConnectionParams.AAD_CREDENTIAL, aadCredential.getText());
            element.setProperty(AzStorageConnectionParams.ENDPOINT_URL, endpointUrl.getText());
            element.setProperty(AzStorageConnectionParams.QUEUE_NAME, queueName.getText());
        } else { // if (auth.equals(AzStorageConnectionParams.AUTHTYPE_CONNECTION_STRING)) {
            element.setProperty(AzStorageConnectionParams.CONNECTION_STRING, connectionString.getText());
            element.setProperty(AzStorageConnectionParams.QUEUE_NAME, queueName.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        authType.setText(AzStorageConnectionParams.AUTHTYPE_CONNECTION_STRING);
        connectionString.setText("");
        defaultEndpointsProtocol.setText("https");
        accountName.setText("");
        endpointSuffix.setText("core.windows.net");
        queueName.setText("");
        storageKey.setText("");
        aadCredential.setText("");
        sasToken.setText("");
        endpointUrl.setText("");
    }

    @Override
    public String getLabelResource() {
        return null; // $NON-NLS-1$
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param element
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);

        defaultEndpointsProtocol.setText(element.getPropertyAsString(AzStorageConnectionParams.DEFAULT_ENDPOINTS_PROTOCOL));
        accountName.setText(element.getPropertyAsString(AzStorageConnectionParams.ACCOUNT_NAME));
        endpointSuffix.setText(element.getPropertyAsString(AzStorageConnectionParams.ENDPOINT_SUFFIX));
        queueName.setText(element.getPropertyAsString(AzStorageConnectionParams.QUEUE_NAME));

        String authTypeValue = element.getPropertyAsString(AzStorageConnectionParams.AUTH_TYPE);
        if (authTypeValue.equals(AzStorageConnectionParams.AUTHTYPE_AAD)) {
            authTypeValue = AzStorageConnectionParams.AUTHTYPE_ENTRAID;
        }
        authType.setText(authTypeValue);

        toggleAuthTypeValue();
        storageKey.setText(element.getPropertyAsString(AzStorageConnectionParams.STORAGE_KEY));
        connectionString.setText(element.getPropertyAsString(AzStorageConnectionParams.CONNECTION_STRING));
        aadCredential.setText(element.getPropertyAsString(AzStorageConnectionParams.AAD_CREDENTIAL));
        sasToken.setText(element.getPropertyAsString(AzStorageConnectionParams.SAS_TOKEN));

        endpointUrl.setText(element.getPropertyAsString(AzStorageConnectionParams.ENDPOINT_URL));
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        
        VerticalPanel panel = new VerticalPanel(0, 0);

        panel.setBorder(BorderFactory.createTitledBorder("Storage Connection"));

        createAuthTypePanel(panel);

        // Connection string
        createConnectionStringPanel(panel);

        // AAD
        createAadCredentialPanel(panel);

        // Storage Key
        createStorageKeyPanel(panel);

        // Connection string, AAD, SAS
        createEndpointUrlPanel(panel);

        // Connection string, AAD, Storage key, SAS
        createQueueNamePanel(panel);

        // SAS
        createSasTokenPanel(panel);

        add(panel, BorderLayout.CENTER);
    }

    private void createStorageKeyPanel(VerticalPanel panel) {
        defaultEndpointsProtocol = new JLabeledChoice("", PROTOCOL_LABELS);
        defaultEndpointsProtocol.setName(AzStorageConnectionParams.DEFAULT_ENDPOINTS_PROTOCOL);
        defaultEndpointsProtocol.addChangeListener(this);

        JPanel defaultEndpointsProtocolPanel = new JPanel(new BorderLayout(5, 0));
        defaultEndpointsProtocolPanel.add(new JLabel("Default Endpoints Protocol:"), BorderLayout.WEST);
        defaultEndpointsProtocolPanel.add(defaultEndpointsProtocol, BorderLayout.CENTER);

        accountName = new JLabeledTextField("Account Name:");
        accountName.setName(AzStorageConnectionParams.ACCOUNT_NAME);
        accountName.addChangeListener(this);

        storageKey = new JLabeledPasswordField("Storage Key:");
        storageKey.setName(AzStorageConnectionParams.STORAGE_KEY);
        storageKey.addChangeListener(this);

        endpointSuffix = new JLabeledTextField("Endpoint Suffix:");
        endpointSuffix.setName(AzStorageConnectionParams.ENDPOINT_SUFFIX);
        endpointSuffix.addChangeListener(this);

        storageKeyPanel.add(defaultEndpointsProtocolPanel);
        storageKeyPanel.add(accountName);
        storageKeyPanel.add(storageKey);
        storageKeyPanel.add(endpointSuffix);
        storageKeyPanel.add(Box.createVerticalStrut(0));

        panel.add(storageKeyPanel);
    }

    private void createConnectionStringPanel(VerticalPanel panel) {
        connectionString = new JLabeledTextField("Connection string:");
        connectionString.setName(AzStorageConnectionParams.CONNECTION_STRING);
        connectionString.setLocation(10, 20);

        connectionStringPanel.add(connectionString);
        connectionStringPanel.add(Box.createVerticalStrut(0));

        panel.add(connectionStringPanel);
    }

    private void createAadCredentialPanel(VerticalPanel panel) {
        aadCredential = new JLabeledTextField("Variable Name of credential Defined in Microsoft Entra ID Credential:");
        aadCredential.setName(AzStorageConnectionParams.AAD_CREDENTIAL);
        aadCredentialPanel.add(aadCredential);
        aadCredentialPanel.add(Box.createVerticalStrut(0));

        panel.add(aadCredentialPanel);
    }

    private void createSasTokenPanel(VerticalPanel panel) {
        sasToken = new JLabeledPasswordField("SAS token:");
        sasToken.setName(AzStorageConnectionParams.SAS_TOKEN);
        sasTokenPanel.add(sasToken);
        sasTokenPanel.add(Box.createVerticalStrut(0));

        panel.add(sasTokenPanel);
    }

    private void createEndpointUrlPanel(VerticalPanel panel) {
        endpointUrl = new JLabeledTextField("Endpoint URL:");
        endpointUrl.setName(AzStorageConnectionParams.ENDPOINT_URL);
        endpointUrlPanel.add(endpointUrl);
        endpointUrlPanel.add(Box.createVerticalStrut(0));

        panel.add(endpointUrlPanel);
    }

    private void createQueueNamePanel(VerticalPanel panel) {
        queueName = new JLabeledTextField("Queue name:");
        queueName.setName(AzStorageConnectionParams.QUEUE_NAME);

        queueNamePanel.add(queueName);
        queueNamePanel.add(Box.createVerticalStrut(0));

        panel.add(queueNamePanel);
    }

    private void createAuthTypePanel(VerticalPanel panel) {
        JLabel authTypeLabel = new JLabel("Auth type:");

        authType = new JLabeledChoice("", AUTH_TYPE_LABELS);
        authType.setName(AzStorageConnectionParams.AUTH_TYPE);
        authType.addChangeListener(this);

        authTypePanel.add(authTypeLabel, BorderLayout.WEST);
        authTypePanel.add(authType, BorderLayout.CENTER);
        authTypePanel.add(Box.createVerticalStrut(verticalStrut), BorderLayout.SOUTH);

        panel.add(authTypePanel);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource().equals(authType)) {
            toggleAuthTypeValue();
        } else if (event.getSource().equals(defaultEndpointsProtocol) || event.getSource().equals(accountName) || event.getSource().equals(storageKey) || event.getSource().equals(endpointSuffix)) {
            updateConnectionString();
        }
    }

    /**
     * Update connection string.
     */
    private void updateConnectionString() {
        String conn
            = "DefaultEndpointsProtocol=".concat(defaultEndpointsProtocol.getText().trim()).concat(";")
            .concat("AccountName=").concat(accountName.getText().trim()).concat(";")
            .concat("AccountKey=").concat(storageKey.getText().trim()).concat(";")
            .concat("EndpointSuffix=").concat(endpointSuffix.getText().trim()).concat(";");
        connectionString.setText(conn);
    }

    /**
     * Visualize selected auth type.
     */
    private void toggleAuthTypeValue() {
        String auth = authType.getText();
        if (auth.equals(AzStorageConnectionParams.AUTHTYPE_KEY)) {
            connectionStringPanel.setVisible(false);
            aadCredentialPanel.setVisible(false);
            storageKeyPanel.setVisible(true);
            endpointUrlPanel.setVisible(false);
            queueNamePanel.setVisible(true);
            sasTokenPanel.setVisible(false);
        } else if (auth.equals(AzStorageConnectionParams.AUTHTYPE_SAS)) {
            connectionStringPanel.setVisible(false);
            aadCredentialPanel.setVisible(false);
            storageKeyPanel.setVisible(false);
            endpointUrlPanel.setVisible(true);
            queueNamePanel.setVisible(true);
            sasTokenPanel.setVisible(true);
        } else if (auth.equals(AzStorageConnectionParams.AUTHTYPE_ENTRAID) || auth.equals(AzStorageConnectionParams.AUTHTYPE_AAD)) {
            connectionStringPanel.setVisible(false);
            aadCredentialPanel.setVisible(true);
            storageKeyPanel.setVisible(false);
            endpointUrlPanel.setVisible(true);
            queueNamePanel.setVisible(true);
            sasTokenPanel.setVisible(false);
        } else { //if (auth.equals(AzStorageConnectionParams.AUTHTYPE_CONNECTION_STRING)) {
            connectionStringPanel.setVisible(true);
            aadCredentialPanel.setVisible(false);
            storageKeyPanel.setVisible(false);
            endpointUrlPanel.setVisible(false);
            queueNamePanel.setVisible(true);
            sasTokenPanel.setVisible(false);
        }
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