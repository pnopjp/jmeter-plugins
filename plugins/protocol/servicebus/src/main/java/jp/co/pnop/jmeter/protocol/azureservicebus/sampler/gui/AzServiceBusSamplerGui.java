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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azureservicebus.common.AzServiceBusClientParams;
import jp.co.pnop.jmeter.protocol.azureservicebus.common.gui.AzServiceBusClientParamsPanel;
import jp.co.pnop.jmeter.protocol.azureservicebus.sampler.AzServiceBusSampler;

public class AzServiceBusSamplerGui extends AbstractSamplerGui implements PropertyChangeListener, ChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusSamplerGui.class);

    private JCheckBox createTransaction;
    private JLabeledTextField createTransactionName;
    private JRadioButton continueTransaction;
    private JRadioButton commitTransaction;
    private JRadioButton rollbackTransaction;

    private AzServiceBusClientParamsPanel sbclientPanel = new AzServiceBusClientParamsPanel();
    private AzServiceBusMessagesPanel messagesPanel = new AzServiceBusMessagesPanel();

    private ButtonGroup transactionStatusGroup = new ButtonGroup();

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzServiceBusSamplerGui() {
        init();
        classCount.incrementAndGet();
        trace("AzServiceBusSamplerGui()");
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

        createTransaction.setSelected(element.getPropertyAsBoolean(AzServiceBusSampler.CREATE_TRANSACTION));
        createTransactionName.setText(element.getPropertyAsString(AzServiceBusSampler.CREATE_TRANSACTION_NAME));
        continueTransaction.setSelected(element.getPropertyAsBoolean(AzServiceBusSampler.CONTINUE_TRANSACTION));
        commitTransaction.setSelected(element.getPropertyAsBoolean(AzServiceBusSampler.COMMIT_TRANSACTION));
        rollbackTransaction.setSelected(element.getPropertyAsBoolean(AzServiceBusSampler.ROLLBACK_TRANSACTION));
        sbclientPanel.configure((TestElement)element.getProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS).getObjectValue());
        messagesPanel.configure((TestElement)element.getProperty(AzServiceBusSampler.MESSAGES).getObjectValue());
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

        sampler.setProperty(new TestElementProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS, sbclientPanel.createTestElement()));
        sampler.setProperty(AzServiceBusSampler.CREATE_TRANSACTION, createTransaction.isSelected());
        sampler.setProperty(AzServiceBusSampler.CREATE_TRANSACTION_NAME, createTransactionName.getText());
        sampler.setProperty(AzServiceBusSampler.CONTINUE_TRANSACTION, continueTransaction.isSelected());
        sampler.setProperty(AzServiceBusSampler.COMMIT_TRANSACTION, commitTransaction.isSelected());
        sampler.setProperty(AzServiceBusSampler.ROLLBACK_TRANSACTION, rollbackTransaction.isSelected());
        sampler.setProperty(new TestElementProperty(AzServiceBusSampler.MESSAGES, messagesPanel.createTestElement()));
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        sbclientPanel.clearGui();
        createTransaction.setSelected(false);
        createTransactionName.setText("");
        createTransactionName.setEnabled(false);
        continueTransaction.setSelected(true);
        continueTransaction.setEnabled(false);
        commitTransaction.setSelected(false);
        commitTransaction.setEnabled(false);
        rollbackTransaction.setSelected(false);
        rollbackTransaction.setEnabled(false);
        messagesPanel.clear();
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "Azure Service Bus Sampler";
    }

    private JPanel createCreateTransactionPanel() {
        JPanel createTransactionPanel = new JPanel(new BorderLayout(0, 5));
        JLabel createTransactionLabel = new JLabel("Create transaction before sending messages:");
        createTransactionLabel.setLabelFor(createTransaction);

        createTransaction = new JCheckBox();
        createTransaction.setName(AzServiceBusSampler.CREATE_TRANSACTION);
        createTransaction.addChangeListener(this);

        createTransactionPanel.add(createTransactionLabel, BorderLayout.WEST);
        createTransactionPanel.add(createTransaction, BorderLayout.CENTER);

        createTransactionName = new JLabeledTextField("Variable name for created transaction:");
        createTransactionName.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(createTransactionPanel, BorderLayout.WEST);
        panel.add(createTransactionName, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransactionStatusPanel() {
        continueTransaction = new JRadioButton(AzServiceBusSampler.CONTINUE_TRANSACTION);
        continueTransaction.setText("Continue transaction");
        transactionStatusGroup.add(continueTransaction);
        
        commitTransaction = new JRadioButton(AzServiceBusSampler.COMMIT_TRANSACTION);
        commitTransaction.setText("Commit transaction after sending messages");
        transactionStatusGroup.add(commitTransaction);

        rollbackTransaction = new JRadioButton(AzServiceBusSampler.ROLLBACK_TRANSACTION);
        rollbackTransaction.setText("Rollback transaction before sending messages");
        transactionStatusGroup.add(rollbackTransaction);

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(continueTransaction);
        panel.add(commitTransaction);
        panel.add(rollbackTransaction);

        return panel;
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(messagesPanel, BorderLayout.CENTER);
        return panel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        VerticalPanel servicebusPanel = new VerticalPanel();
        
        servicebusPanel.add(sbclientPanel);
        sbclientPanel.addPropertyChangeListener(this);
        servicebusPanel.add(createCreateTransactionPanel());
        servicebusPanel.add(createTransactionStatusPanel());

        mainPanel.add(servicebusPanel, BorderLayout.NORTH);
        mainPanel.add(createMessagesPanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(AzServiceBusClientParams.CONNECTION_TYPE)) {
            Boolean status = e.getNewValue().equals(AzServiceBusClientParams.CONNECTION_TYPE_DEFINED_TRANSACTION);
            continueTransaction.setEnabled(status);
            commitTransaction.setEnabled(status);
            rollbackTransaction.setEnabled(status);
            createTransaction.setEnabled(!status);
            if (status) {
                createTransaction.setSelected(false);
                createTransactionName.setEnabled(false);
                createTransactionName.setText("");
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(createTransaction)) {
            toggleCreateTransaction();
        }
    }

    /*
     * Updated "Create transaction before sending messages" checkbox.
     */
    private void toggleCreateTransaction() {
        Boolean status = createTransaction.isSelected();
        createTransactionName.setEnabled(status);
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
