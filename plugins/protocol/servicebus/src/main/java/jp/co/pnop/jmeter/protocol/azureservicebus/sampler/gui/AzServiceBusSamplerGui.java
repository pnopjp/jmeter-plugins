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
package jp.co.pnop.jmeter.protocol.azureservicebus.sampler.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azureservicebus.sampler.AzServiceBusSampler;

public class AzServiceBusSamplerGui extends AbstractSamplerGui implements ChangeListener {
    private static final long serialVersionUID = 1L;
    //private static final Logger log = LoggerFactory.getLogger(AzServiceBusSamplerGui.class);

    private JLabeledTextField namespaceName;
    private String[] PROTOCOL_LABELS = {
        AzServiceBusSampler.PROTOCOL_AMQP,
        AzServiceBusSampler.PROTOCOL_AMQP_OVER_WEBSOCKETS
    };
    private String[] AUTH_TYPE_LABELS = {
        AzServiceBusSampler.AUTHTYPE_SAS,
        AzServiceBusSampler.AUTHTYPE_AAD
    };
    private JPanel authPanel;
    private JLabeledChoice authType;
    private JLabeledTextField sharedAccessKeyName;
    private JPasswordField sharedAccessKey;
    private JLabeledTextField aadCredential;
    private JRadioButton destTypeQueue;
    private JRadioButton destTypeTopic;
    private JLabeledTextField queueName;
    private JLabeledChoice protocol;
    private AzServiceBusMessagesPanel messagesPanel = new AzServiceBusMessagesPanel(); // $NON-NLS-1$

    private ButtonGroup destTypeGroup = new ButtonGroup();

    public AzServiceBusSamplerGui() {
        init();
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
        namespaceName.setText(element.getPropertyAsString(AzServiceBusSampler.NAMESPACE_NAME));
        authType.setText(element.getPropertyAsString(AzServiceBusSampler.AUTH_TYPE));
        toggleAuthTypeValue();
        sharedAccessKeyName.setText(element.getPropertyAsString(AzServiceBusSampler.SHARED_ACCESS_KEY_NAME));
        sharedAccessKey.setText(element.getPropertyAsString(AzServiceBusSampler.SHARED_ACCESS_KEY));
        aadCredential.setText(element.getPropertyAsString(AzServiceBusSampler.AAD_CREDENTIAL));
        final String destType = element.getPropertyAsString(AzServiceBusSampler.DEST_TYPE);
        if (destType.equals(AzServiceBusSampler.DEST_TYPE_TOPIC)) {
            destTypeTopic.setSelected(true);
        } else {
            destTypeQueue.setSelected(true);
        }
        queueName.setText(element.getPropertyAsString(AzServiceBusSampler.QUEUE_NAME));
        protocol.setText(element.getPropertyAsString(AzServiceBusSampler.PROTOCOL));
        messagesPanel.configure((TestElement) element.getProperty(AzServiceBusSampler.MESSAGES).getObjectValue());
    }

    @Override
    public TestElement createTestElement() {
        AzServiceBusSampler sampler = new AzServiceBusSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        super.configureTestElement(sampler);
        sampler.setProperty(AzServiceBusSampler.NAMESPACE_NAME, namespaceName.getText());
        sampler.setProperty(AzServiceBusSampler.AUTH_TYPE, authType.getText());
        if (authType.getText() == AzServiceBusSampler.AUTHTYPE_AAD) {
            sampler.setProperty(AzServiceBusSampler.AAD_CREDENTIAL, aadCredential.getText());
        } else { // AUTHTYPE_SAS
            sampler.setProperty(AzServiceBusSampler.SHARED_ACCESS_KEY_NAME, sharedAccessKeyName.getText());
            sampler.setProperty(AzServiceBusSampler.SHARED_ACCESS_KEY, new String(sharedAccessKey.getPassword()));
        }
        String destType = "";
        if (destTypeTopic.isSelected()) {
            destType = AzServiceBusSampler.DEST_TYPE_TOPIC;
        } else {
            destType = AzServiceBusSampler.DEST_TYPE_QUEUE;
        };
        sampler.setProperty(AzServiceBusSampler.DEST_TYPE, destType);
        sampler.setProperty(AzServiceBusSampler.QUEUE_NAME, queueName.getText());
        sampler.setProperty(AzServiceBusSampler.PROTOCOL, protocol.getText());
        sampler.setProperty(new TestElementProperty(AzServiceBusSampler.MESSAGES, messagesPanel.createTestElement()));
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        namespaceName.setText("");
        authType.setText(AzServiceBusSampler.AUTHTYPE_SAS);
        sharedAccessKeyName.setText("");
        sharedAccessKey.setText("");
        aadCredential.setText("");
        destTypeQueue.setSelected(true);
        destTypeTopic.setSelected(false);
        queueName.setText("");
        protocol.setText(AzServiceBusSampler.PROTOCOL_AMQP);
        messagesPanel.clear();
    }

    @Override
    public String getLabelResource() {
        return null; // $NON-NLS-1$
    }

    public String getStaticLabel() {
        return "Azure Service Bus Sampler";
    }

    private JPanel createNamespaceNamePanel() {
        namespaceName = new JLabeledTextField("Service Bus Namespace:");
        namespaceName.setName(AzServiceBusSampler.NAMESPACE_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(namespaceName);

        return panel;
    }

    private JPanel createSharedAccessKeyNamePanel() {
        sharedAccessKeyName = new JLabeledTextField("Shared Access Policy:");
        sharedAccessKeyName.setName(AzServiceBusSampler.SHARED_ACCESS_KEY_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(sharedAccessKeyName);

        return panel;
    }

    private JPanel createSharedAccessKeyPanel() {
        sharedAccessKey = new JPasswordField();
        sharedAccessKey.setName(AzServiceBusSampler.SHARED_ACCESS_KEY);

        JLabel label = new JLabel("Shared Access Key:");
        label.setLabelFor(sharedAccessKey);
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(sharedAccessKey, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAadCredentialPanel() {
        aadCredential = new JLabeledTextField("Variable Name of credential declared in Azure AD Credential:");
        aadCredential.setName(AzServiceBusSampler.AAD_CREDENTIAL);
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
        destTypeQueue = new JRadioButton(AzServiceBusSampler.DEST_TYPE_QUEUE);
        destTypeTopic = new JRadioButton(AzServiceBusSampler.DEST_TYPE_TOPIC);
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
        queueName.setName(AzServiceBusSampler.QUEUE_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(queueName);

        return panel;
    }

    private JPanel createProtocolPanel() {
        JLabel protocolLabel = new JLabel("Protocol:");

        protocol = new JLabeledChoice("", PROTOCOL_LABELS);
        protocol.setName(AzServiceBusSampler.PROTOCOL);
        protocol.addChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(protocolLabel, BorderLayout.WEST);
        panel.add(protocol, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(messagesPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAuthTypePanel() {
        JLabel authTypeLabel = new JLabel("Auth type:");

        authType = new JLabeledChoice("", AUTH_TYPE_LABELS);
        authType.setName(AzServiceBusSampler.AUTH_TYPE);
        authType.addChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Auth Configuration"));
        panel.add(authTypeLabel, BorderLayout.WEST);
        panel.add(authType, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuthPanel() {
        authPanel = new JPanel(new CardLayout());
        authPanel.add(createSharedAccessSignaturePanel(), AzServiceBusSampler.AUTHTYPE_SAS);
        authPanel.add(createAadCredentialPanel(), AzServiceBusSampler.AUTHTYPE_AAD);

        return authPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        VerticalPanel ServiceBusConfigPanel = new VerticalPanel();
        ServiceBusConfigPanel.setBorder(BorderFactory.createTitledBorder("Service Bus Configuration"));
        ServiceBusConfigPanel.add(createNamespaceNamePanel());
        ServiceBusConfigPanel.add(createDestTypePanel());
        ServiceBusConfigPanel.add(createQueueNamePanel());
        ServiceBusConfigPanel.add(createProtocolPanel());
        ServiceBusConfigPanel.add(createAuthTypePanel());
        ServiceBusConfigPanel.add(createAuthPanel());
        mainPanel.add(ServiceBusConfigPanel, BorderLayout.NORTH);
        mainPanel.add(createMessagesPanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource().equals(destTypeQueue) || event.getSource().equals(destTypeTopic)) {
            toggleDestTypeLabel();
        } else if (event.getSource().equals(authType)) {
            toggleAuthTypeValue();
        }
    }

    /**
     * Visualize selected auth type.
     */
    private void toggleDestTypeLabel() {
        String label = "";
        if (destTypeTopic.isSelected()) {
            label = AzServiceBusSampler.DEST_TYPE_TOPIC;
        } else {
            label = AzServiceBusSampler.DEST_TYPE_QUEUE;
        }
        queueName.setLabel(label.concat(" name:"));
    }

    /**
     * Visualize selected auth type.
     */
    private void toggleAuthTypeValue() {
        CardLayout authTypeLayout = (CardLayout) authPanel.getLayout();
        if (authType.getText() == AzServiceBusSampler.AUTHTYPE_SAS) {
            authTypeLayout.show(authPanel, AzServiceBusSampler.AUTHTYPE_SAS);
        } else { // AUTHTYPE_AAD
            authTypeLayout.show(authPanel, AzServiceBusSampler.AUTHTYPE_AAD);
        }
    }

}
