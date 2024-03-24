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

 package jp.co.pnop.jmeter.protocol.azureservicebus.common.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azureservicebus.common.AzServiceBusClientParams;

public class AzServiceBusClientParamsPanel extends AbstractConfigGui implements ChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusClientParamsPanel.class);

    private JRadioButton newConnection;
    private JRadioButton definedConnection;
    private JRadioButton definedTransaction;
    private JLabeledTextField definedConnectionName;
    private JLabeledTextField namespaceName;
    private JRadioButton destTypeQueue;
    private JRadioButton destTypeTopic;
    private JLabeledTextField queueName;
    private JLabeledChoice protocol;
    private String[] PROTOCOL_LABELS = {
        AzServiceBusClientParams.PROTOCOL_AMQP,
        AzServiceBusClientParams.PROTOCOL_AMQP_OVER_WEBSOCKETS
    };
    private JPanel authPanel;
    private JLabeledChoice authType;
    private String[] AUTH_TYPE_LABELS = {
        AzServiceBusClientParams.AUTHTYPE_SAS,
        AzServiceBusClientParams.AUTHTYPE_ENTRAID
    };
    private JLabeledTextField sharedAccessKeyName;
    private JPasswordField sharedAccessKey;
    private JLabeledTextField aadCredential;
    
    private VerticalPanel serviceBusConfigPanel = new VerticalPanel();
    private JPanel connectionTypePanel;

    private ButtonGroup connectionTypeGroup = new ButtonGroup();
    private ButtonGroup destTypeGroup = new ButtonGroup();

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzServiceBusClientParamsPanel() {
        init();
        classCount.incrementAndGet();
        trace("AzServiceBusClientPanel()");
    }

    public AzServiceBusClientParamsPanel(Boolean visibleUseDefinedConnectionTransaction) {
        this();
        connectionTypePanel.setVisible(visibleUseDefinedConnectionTransaction);
    }
    
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        AzServiceBusClientParams producer = new AzServiceBusClientParams();
        modifyTestElement(producer);
        return (TestElement) producer.clone();
    }

    @Override
    public void modifyTestElement(TestElement element) {
        element.clear();
        super.configureTestElement(element);

        String connectionType = "";
        if (definedConnection.isSelected()) {
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_CONNECTION;
            element.setProperty(AzServiceBusClientParams.DEFINED_CONNECTION_NAME, definedConnectionName.getText());
        } else if (definedTransaction.isSelected()) {
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION;
            element.setProperty(AzServiceBusClientParams.DEFINED_CONNECTION_NAME, definedConnectionName.getText());
        } else { // newConnection
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_NEW_CONNECTION;

            element.setProperty(AzServiceBusClientParams.NAMESPACE_NAME, namespaceName.getText());
            element.setProperty(AzServiceBusClientParams.AUTH_TYPE, authType.getText());
            if (authType.getText() == AzServiceBusClientParams.AUTHTYPE_ENTRAID || authType.getText() == AzServiceBusClientParams.AUTHTYPE_AAD) {
                element.setProperty(AzServiceBusClientParams.AAD_CREDENTIAL, aadCredential.getText());
            } else { // AUTHTYPE_SAS
                element.setProperty(AzServiceBusClientParams.SHARED_ACCESS_KEY_NAME, sharedAccessKeyName.getText());
                element.setProperty(AzServiceBusClientParams.SHARED_ACCESS_KEY, new String(sharedAccessKey.getPassword()));
            }
            String destType = "";
            if (destTypeTopic.isSelected()) {
                destType = AzServiceBusClientParams.DEST_TYPE_TOPIC;
            } else {
                destType = AzServiceBusClientParams.DEST_TYPE_QUEUE;
            };
            element.setProperty(AzServiceBusClientParams.DEST_TYPE, destType);
            element.setProperty(AzServiceBusClientParams.QUEUE_NAME, queueName.getText());
            element.setProperty(AzServiceBusClientParams.PROTOCOL, protocol.getText());
        }
        element.setProperty(AzServiceBusClientParams.CONNECTION_TYPE, connectionType);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        newConnection.setSelected(true);
        definedConnection.setSelected(false);
        definedTransaction.setSelected(false);
        definedConnectionName.setText("");
        namespaceName.setText("");
        destTypeQueue.setSelected(true);
        destTypeTopic.setSelected(false);
        queueName.setText("");
        protocol.setText(AzServiceBusClientParams.PROTOCOL_AMQP);
        authType.setText(AzServiceBusClientParams.AUTHTYPE_SAS);
        sharedAccessKeyName.setText("");
        sharedAccessKey.setText("");
        aadCredential.setText("");
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

        final String connectionType = element.getPropertyAsString(AzServiceBusClientParams.CONNECTION_TYPE);
        if (connectionType.equals(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_CONNECTION)) {
            definedConnection.setSelected(true);
            definedConnectionName.setText(element.getPropertyAsString(AzServiceBusClientParams.DEFINED_CONNECTION_NAME));
        } else if (connectionType.equals(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION)) {
            definedTransaction.setSelected(true);
            definedConnectionName.setText(element.getPropertyAsString(AzServiceBusClientParams.DEFINED_CONNECTION_NAME));
        } else { // CONNECTION_TYPE_NEW_CONNECTION
            newConnection.setSelected(true);
        }
        namespaceName.setText(element.getPropertyAsString(AzServiceBusClientParams.NAMESPACE_NAME));
        final String destType = element.getPropertyAsString(AzServiceBusClientParams.DEST_TYPE);
        if (destType.equals(AzServiceBusClientParams.DEST_TYPE_TOPIC)) {
            destTypeTopic.setSelected(true);
        } else {
            destTypeQueue.setSelected(true);
        }
        queueName.setText(element.getPropertyAsString(AzServiceBusClientParams.QUEUE_NAME));
        protocol.setText(element.getPropertyAsString(AzServiceBusClientParams.PROTOCOL));

        String authTypeValue = element.getPropertyAsString(AzServiceBusClientParams.AUTH_TYPE);
        if (authTypeValue.equals(AzServiceBusClientParams.AUTHTYPE_AAD)) {
            authTypeValue = AzServiceBusClientParams.AUTHTYPE_ENTRAID;
        }
        authType.setText(authTypeValue);
        
        toggleAuthTypeValue();
        sharedAccessKeyName.setText(element.getPropertyAsString(AzServiceBusClientParams.SHARED_ACCESS_KEY_NAME));
        sharedAccessKey.setText(element.getPropertyAsString(AzServiceBusClientParams.SHARED_ACCESS_KEY));
        aadCredential.setText(element.getPropertyAsString(AzServiceBusClientParams.AAD_CREDENTIAL));
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));

        VerticalPanel panel = new VerticalPanel();

        panel.setBorder(BorderFactory.createTitledBorder("Service Bus Configuration"));
        connectionTypePanel = createConnectionTypePanel();
        panel.add(connectionTypePanel);
        panel.add(createDefinedTransactionNamePanel());

        serviceBusConfigPanel.add(createNamespaceNamePanel());
        serviceBusConfigPanel.add(createDestTypePanel());
        serviceBusConfigPanel.add(createQueueNamePanel());
        serviceBusConfigPanel.add(createProtocolPanel());
        serviceBusConfigPanel.add(createAuthTypePanel());
        serviceBusConfigPanel.add(createAuthPanel());
        panel.add(serviceBusConfigPanel);

        add(panel, BorderLayout.CENTER);
    }

    private JPanel createConnectionTypePanel() {
        JLabel connectionTypeLabel = new JLabel("Connection/Transaction:");
        newConnection = new JRadioButton(AzServiceBusClientParams.CONNECTION_TYPE_NEW_CONNECTION);
        definedConnection = new JRadioButton(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_CONNECTION);
        definedTransaction = new JRadioButton(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION);

        newConnection.addChangeListener(this);
        definedConnection.addChangeListener(this);
        definedTransaction.addChangeListener(this);

        JPanel panel = new HorizontalPanel();
        panel.add(connectionTypeLabel);
        panel.add(newConnection);
        panel.add(definedConnection);
        panel.add(definedTransaction);

        connectionTypeGroup.add(newConnection);
        connectionTypeGroup.add(definedConnection);
        connectionTypeGroup.add(definedTransaction);

        return panel;
    }

    private JPanel createDefinedTransactionNamePanel() {
        definedConnectionName = new JLabeledTextField("Variable Name of connection Defined in Azure Service Bus Sampler:");
        definedConnectionName.setName(AzServiceBusClientParams.DEFINED_CONNECTION_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(definedConnectionName);

        return panel;
    }

    private JPanel createNamespaceNamePanel() {
        namespaceName = new JLabeledTextField("Service Bus Namespace:");
        namespaceName.setName(AzServiceBusClientParams.NAMESPACE_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(namespaceName);

        return panel;
    }

    private JPanel createSharedAccessKeyNamePanel() {
        sharedAccessKeyName = new JLabeledTextField("Shared Access Policy:");
        sharedAccessKeyName.setName(AzServiceBusClientParams.SHARED_ACCESS_KEY_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(sharedAccessKeyName);

        return panel;
    }

    private JPanel createSharedAccessKeyPanel() {
        sharedAccessKey = new JPasswordField();
        sharedAccessKey.setName(AzServiceBusClientParams.SHARED_ACCESS_KEY);

        JLabel label = new JLabel("Shared Access Key:");
        label.setLabelFor(sharedAccessKey);
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(sharedAccessKey, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAadCredentialPanel() {
        aadCredential = new JLabeledTextField("Variable Name of credential Defined in Microsoft Entra ID Credential:");
        aadCredential.setName(AzServiceBusClientParams.AAD_CREDENTIAL);
        JPanel panel = new VerticalPanel();
        panel.add(aadCredential);
        
        return panel;
    }

    private JPanel createSharedAccessSignaturePanel() {
        JPanel panel = new VerticalPanel();
        panel.add(createSharedAccessKeyNamePanel());
        panel.add(createSharedAccessKeyPanel());

        return panel;
    }

    private JPanel createDestTypePanel() {
        JLabel destTypeLabel = new JLabel("Send messages to:");
        destTypeQueue = new JRadioButton(AzServiceBusClientParams.DEST_TYPE_QUEUE);
        destTypeTopic = new JRadioButton(AzServiceBusClientParams.DEST_TYPE_TOPIC);
        destTypeQueue.addChangeListener(this);
        destTypeTopic.addChangeListener(this);

        JPanel panel = new HorizontalPanel();
        panel.add(destTypeLabel);
        panel.add(destTypeQueue);
        panel.add(destTypeTopic);
        destTypeGroup.add(destTypeQueue);
        destTypeGroup.add(destTypeTopic);

        return panel;
    }

    private JPanel createQueueNamePanel() {
        queueName = new JLabeledTextField("Queue name:");
        queueName.setName(AzServiceBusClientParams.QUEUE_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(queueName);

        return panel;
    }

    private JPanel createProtocolPanel() {
        JLabel protocolLabel = new JLabel("Protocol:");

        protocol = new JLabeledChoice("", PROTOCOL_LABELS);
        protocol.setName(AzServiceBusClientParams.PROTOCOL);
        protocol.addChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(protocolLabel, BorderLayout.WEST);
        panel.add(protocol, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuthTypePanel() {
        JLabel authTypeLabel = new JLabel("Auth type:");

        authType = new JLabeledChoice("", AUTH_TYPE_LABELS);
        authType.setName(AzServiceBusClientParams.AUTH_TYPE);
        authType.addChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Auth Configuration"));
        panel.add(authTypeLabel, BorderLayout.WEST);
        panel.add(authType, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuthPanel() {
        authPanel = new JPanel(new CardLayout());
        authPanel.add(createSharedAccessSignaturePanel(), AzServiceBusClientParams.AUTHTYPE_SAS);
        authPanel.add(createAadCredentialPanel(), AzServiceBusClientParams.AUTHTYPE_ENTRAID);

        return authPanel;
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource().equals(destTypeQueue) || event.getSource().equals(destTypeTopic)) {
            toggleDestTypeValue();
        } else if (event.getSource().equals(authType)) {
            toggleAuthTypeValue();
        } else if (event.getSource().equals(newConnection) || event.getSource().equals(definedConnection) || event.getSource().equals(definedTransaction)) {
            toggleConnectionTypeValue();
       }
    }

    /**
     * Selected destination type.
     */
    private void toggleDestTypeValue() {
        String label = "";
        if (destTypeTopic.isSelected()) {
            label = AzServiceBusClientParams.DEST_TYPE_TOPIC;
        } else {
            label = AzServiceBusClientParams.DEST_TYPE_QUEUE;
        }
        queueName.setLabel(label.concat(" name:"));
    }

    /**
     * Visualize selected auth type.
     */
    private void toggleAuthTypeValue() {
        CardLayout authTypeLayout = (CardLayout) authPanel.getLayout();
        if (authType.getText() == AzServiceBusClientParams.AUTHTYPE_SAS) {
            authTypeLayout.show(authPanel, AzServiceBusClientParams.AUTHTYPE_SAS);
        } else { // AUTHTYPE_ENTRAID or AUTHTYPE_AAD
            authTypeLayout.show(authPanel, AzServiceBusClientParams.AUTHTYPE_ENTRAID);
        }
    }

    /**
     * Selected connection type.
     */
    private void toggleConnectionTypeValue() {
        String label = "";
        String connectionType = "";
        if (newConnection.isSelected()) {
            definedConnectionName.setVisible(false);
            serviceBusConfigPanel.setVisible(true);
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_NEW_CONNECTION;
        } else {
            if (definedConnection.isSelected()) {
                label = AzServiceBusClientParams.CONNECTION_TYPE_CONNECTION;
                connectionType = AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_CONNECTION;
            } else if (definedTransaction.isSelected()) {
                label = AzServiceBusClientParams.CONNECTION_TYPE_TRANSACTION;
                connectionType = AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION;
            }
            definedConnectionName.setVisible(true);
            serviceBusConfigPanel.setVisible(false);
            definedConnectionName.setLabel("Variable Name of ".concat(label).concat(" Defined in Azure Service Bus Sampler:"));
        }
        this.firePropertyChange(AzServiceBusClientParams.CONNECTION_TYPE, "", connectionType);
    }

    public String getSelectedConnectionType() {
        String connectionType = "";
        if (definedConnection.isSelected()) {
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_CONNECTION;
        } else if (definedTransaction.isSelected()) {
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION;
        } else {
            connectionType = AzServiceBusClientParams.CONNECTION_TYPE_NEW_CONNECTION;
        }
        return connectionType;
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