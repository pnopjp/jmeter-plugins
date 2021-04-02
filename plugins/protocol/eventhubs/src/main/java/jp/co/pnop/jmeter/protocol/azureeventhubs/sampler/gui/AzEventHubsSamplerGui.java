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
package jp.co.pnop.jmeter.protocol.azureeventhubs.sampler.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BorderFactory;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.amqp.config.gui.AzAmqpMessagesPanel;
import jp.co.pnop.jmeter.protocol.azureeventhubs.sampler.AzEventHubsSampler;

public class AzEventHubsSamplerGui extends AbstractSamplerGui implements ChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzEventHubsSamplerGui.class);

    private JLabeledTextField namespaceName;
    private String[] AUTH_TYPE_LABELS = {
        AzEventHubsSampler.AUTHTYPE_SAS,
        AzEventHubsSampler.AUTHTYPE_AAD
    };
    private JLabel authTypeLabel;
    private JPanel authPanel;
    private JLabeledChoice authType;
    private JLabeledTextField sharedAccessKeyName;
    private JPasswordField sharedAccessKey;
    private JLabeledTextField aadCredential;
    private JLabeledTextField eventHubName;
    private JLabel partitionTypeLabel;
    private String[] PARTITION_TYPE_LABELS = {
        AzEventHubsSampler.PARTITION_TYPE_NOT_SPECIFIED,
        AzEventHubsSampler.PARTITION_TYPE_ID,
        AzEventHubsSampler.PARTITION_TYPE_KEY
    };
    private JLabeledChoice partitionType;
    private JTextField partitionValue;
    private AzAmqpMessagesPanel messagesPanel = new AzAmqpMessagesPanel("Event data"); // $NON-NLS-1$

    public AzEventHubsSamplerGui() {
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
        namespaceName.setText(element.getPropertyAsString(AzEventHubsSampler.NAMESPACE_NAME));
        authType.setText(element.getPropertyAsString(AzEventHubsSampler.AUTH_TYPE));
        toggleAuthTypeValue();
        sharedAccessKeyName.setText(element.getPropertyAsString(AzEventHubsSampler.SHARED_ACCESS_KEY_NAME));
        sharedAccessKey.setText(element.getPropertyAsString(AzEventHubsSampler.SHARED_ACCESS_KEY));
        aadCredential.setText(element.getPropertyAsString(AzEventHubsSampler.AAD_CREDENTIAL));
        eventHubName.setText(element.getPropertyAsString(AzEventHubsSampler.EVENT_HUB_NAME));
        partitionType.setText(element.getPropertyAsString(AzEventHubsSampler.PARTITION_TYPE));
        togglePartitionValue();
        partitionValue.setText(element.getPropertyAsString(AzEventHubsSampler.PARTITION_VALUE));
        messagesPanel.configure((TestElement) element.getProperty(AzEventHubsSampler.MESSAGES).getObjectValue());
    }

    @Override
    public TestElement createTestElement() {
        AzEventHubsSampler sampler = new AzEventHubsSampler();
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
        sampler.setProperty(AzEventHubsSampler.NAMESPACE_NAME, namespaceName.getText());
        sampler.setProperty(AzEventHubsSampler.AUTH_TYPE, authType.getText());
        if (authType.getText() == AzEventHubsSampler.AUTHTYPE_AAD) {
            sampler.setProperty(AzEventHubsSampler.AAD_CREDENTIAL, aadCredential.getText());
        } else { // AUTHTYPE_SAS
            sampler.setProperty(AzEventHubsSampler.SHARED_ACCESS_KEY_NAME, sharedAccessKeyName.getText());
            sampler.setProperty(AzEventHubsSampler.SHARED_ACCESS_KEY, new String(sharedAccessKey.getPassword()));
        }
        sampler.setProperty(AzEventHubsSampler.EVENT_HUB_NAME, eventHubName.getText());
        sampler.setProperty(AzEventHubsSampler.PARTITION_TYPE, partitionType.getText());
        sampler.setProperty(AzEventHubsSampler.PARTITION_VALUE, partitionValue.getText());
        sampler.setProperty(new TestElementProperty(AzEventHubsSampler.MESSAGES, messagesPanel.createTestElement()));
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        namespaceName.setText("");
        authType.setText(AzEventHubsSampler.AUTHTYPE_SAS);
        sharedAccessKeyName.setText("");
        sharedAccessKey.setText("");
        aadCredential.setText("");
        eventHubName.setText("");
        partitionType.setText(AzEventHubsSampler.PARTITION_TYPE_NOT_SPECIFIED);
        partitionValue.setText("");
        messagesPanel.clear();
    }

    @Override
    public String getLabelResource() {
        return null; // $NON-NLS-1$
    }

    public String getStaticLabel() {
        return "Azure Event Hubs Sampler";
    }

    private JPanel createNamespaceNamePanel() {
        namespaceName = new JLabeledTextField("Event Hubs Namespace:");
        namespaceName.setName(AzEventHubsSampler.NAMESPACE_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(namespaceName);

        return panel;
    }

    private JPanel createSharedAccessKeyNamePanel() {
        sharedAccessKeyName = new JLabeledTextField("Shared Access Policy:");
        sharedAccessKeyName.setName(AzEventHubsSampler.SHARED_ACCESS_KEY_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(sharedAccessKeyName);

        return panel;
    }

    private JPanel createSharedAccessKeyPanel() {
        sharedAccessKey = new JPasswordField();
        sharedAccessKey.setName(AzEventHubsSampler.SHARED_ACCESS_KEY);

        JLabel label = new JLabel("Shared Access Key:");
        label.setLabelFor(sharedAccessKey);
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(sharedAccessKey, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAadCredentialPanel() {
        aadCredential = new JLabeledTextField("Variable Name of credential declared in Azure AD Credential:");
        aadCredential.setName(AzEventHubsSampler.AAD_CREDENTIAL);
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

    private JPanel createEventHubNamePanel() {
        eventHubName = new JLabeledTextField("Event Hub:");
        eventHubName.setName(AzEventHubsSampler.EVENT_HUB_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(eventHubName);

        return panel;
    }

    private JPanel createPartitionPanel() {
        partitionTypeLabel = new JLabel("Partition:");

        partitionType = new JLabeledChoice("", PARTITION_TYPE_LABELS);
        partitionType.setName(AzEventHubsSampler.PARTITION_TYPE);
        partitionType.addChangeListener(this);

        JPanel partitionTypePanel = new HorizontalPanel();
        partitionTypePanel.add(partitionTypeLabel);
        partitionTypePanel.add(partitionType);

        partitionValue = new JTextField(0);
        partitionValue.setName(AzEventHubsSampler.PARTITION_VALUE);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(partitionTypePanel, BorderLayout.WEST);
        panel.add(partitionValue, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(messagesPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAuthTypePanel() {
        authTypeLabel = new JLabel("Auth type:");

        authType = new JLabeledChoice("", AUTH_TYPE_LABELS);
        authType.setName(AzEventHubsSampler.AUTH_TYPE);
        authType.addChangeListener(this);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Auth Configuration"));
        panel.add(authTypeLabel, BorderLayout.WEST);
        panel.add(authType, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuthPanel() {
        authPanel = new JPanel(new CardLayout());
        authPanel.add(createSharedAccessSignaturePanel(), AzEventHubsSampler.AUTHTYPE_SAS);
        authPanel.add(createAadCredentialPanel(), AzEventHubsSampler.AUTHTYPE_AAD);

        return authPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        VerticalPanel eventHubsConfigPanel = new VerticalPanel();
        eventHubsConfigPanel.setBorder(BorderFactory.createTitledBorder("Event Hubs Configuration"));
        eventHubsConfigPanel.add(createNamespaceNamePanel());
        eventHubsConfigPanel.add(createEventHubNamePanel());
        eventHubsConfigPanel.add(createPartitionPanel());
        eventHubsConfigPanel.add(createAuthTypePanel());
        eventHubsConfigPanel.add(createAuthPanel());
        mainPanel.add(eventHubsConfigPanel, BorderLayout.NORTH);
        mainPanel.add(createMessagesPanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        if (event.getSource().equals(partitionType)) {
            togglePartitionValue();
        } else if (event.getSource().equals(authType)) {
            toggleAuthTypeValue();
        }
    }

    /**
     * Visualize selected auth type.
     */
    private void toggleAuthTypeValue() {
        CardLayout authTypeLayout = (CardLayout) authPanel.getLayout();
        if (authType.getText() == AzEventHubsSampler.AUTHTYPE_SAS) {
            authTypeLayout.show(authPanel, AzEventHubsSampler.AUTHTYPE_SAS);
        } else { // AUTHTYPE_AAD
            authTypeLayout.show(authPanel, AzEventHubsSampler.AUTHTYPE_AAD);
        }
    }

    /**
     * enable/disable fields related to partitionType
     */
    private void togglePartitionValue() {
        partitionValue.setEnabled(partitionType.getText() != AzEventHubsSampler.PARTITION_TYPE_NOT_SPECIFIED);
    }

}
