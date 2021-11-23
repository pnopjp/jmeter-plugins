package jp.co.pnop.jmeter.protocol.azurestoragequeue.sampler.gui;

import java.util.concurrent.atomic.AtomicInteger;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azurestoragequeue.common.AzStorageConnectionParams;
import jp.co.pnop.jmeter.protocol.azurestoragequeue.common.gui.AzStorageConnectionParamsPanel;
import jp.co.pnop.jmeter.protocol.azurestoragequeue.sampler.AzStorageQueueSampler;

public class AzStorageQueueSamplerGui extends AbstractSamplerGui implements ChangeListener {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzStorageQueueSamplerGui.class);
    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    private AzStorageConnectionParamsPanel connectionPanel = new AzStorageConnectionParamsPanel();

    private JLabeledChoice messageType = new JLabeledChoice();
    private String[] MESSAGE_TYPE_LABELS = {
        AzStorageQueueSampler.MESSAGE_TYPE_STRING,
        AzStorageQueueSampler.MESSAGE_TYPE_FILE
    };
    private JLabeledTextArea message;
    private JLabeledTextField messageFile;
    private JLabeledTextField visibilityTimeout;
    private JLabeledTextField timeToLive;
    private JLabeledTextField timeout;
    private JTabbedPane messageTabbedPane;

    public AzStorageQueueSamplerGui() {
        init();
        classCount.incrementAndGet();
        trace("AzStorageQueueSamplerGui()");
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

        connectionPanel.configure((TestElement)element.getProperty(AzStorageConnectionParams.STORAGE_CONNECTION_PARAMS).getObjectValue());
        messageType.setText(element.getPropertyAsString(AzStorageQueueSampler.MESSAGE_TYPE));
        message.setText(element.getPropertyAsString(AzStorageQueueSampler.MESSAGE));
        messageFile.setText(element.getPropertyAsString(AzStorageQueueSampler.MESSAGE_FILE));
        visibilityTimeout.setText(element.getPropertyAsString(AzStorageQueueSampler.VISIBILITY_TIMEOUT));
        timeToLive.setText(element.getPropertyAsString(AzStorageQueueSampler.TIME_TO_LIVE));
        timeout.setText(element.getPropertyAsString(AzStorageQueueSampler.TIMEOUT));
    }

    @Override
    public TestElement createTestElement() {
        AzStorageQueueSampler sampler = new AzStorageQueueSampler();
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

        sampler.setProperty(new TestElementProperty(AzStorageConnectionParams.STORAGE_CONNECTION_PARAMS, connectionPanel.createTestElement()));
        sampler.setProperty(AzStorageQueueSampler.MESSAGE_TYPE, messageType.getText());
        if (messageType.getText().equals(AzStorageQueueSampler.MESSAGE_TYPE_FILE)) {
            sampler.setProperty(AzStorageQueueSampler.MESSAGE_FILE, messageFile.getText());
        } else { // STRING
            sampler.setProperty(AzStorageQueueSampler.MESSAGE, message.getText());
        }
        sampler.setProperty(AzStorageQueueSampler.VISIBILITY_TIMEOUT, visibilityTimeout.getText());
        sampler.setProperty(AzStorageQueueSampler.TIME_TO_LIVE, timeToLive.getText());
        sampler.setProperty(AzStorageQueueSampler.TIMEOUT, timeout.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        connectionPanel.clearGui();
        messageType.setText(AzStorageQueueSampler.MESSAGE_TYPE_STRING);
        message.setText("");
        messageFile.setText("");
        messageFile.setVisible(false);
        visibilityTimeout.setText("");
        timeToLive.setText("");
        timeout.setText("");
        messageTabbedPane.setSelectedIndex(0);
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "Azure Storage Queue Sampler";
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();

        mainPanel.add(connectionPanel, BorderLayout.NORTH);

        messageTabbedPane = new JTabbedPane();
        messageTabbedPane.add("Message", createMessagePanel());
        messageTabbedPane.add("Advanced", createMessageAdvancedPanel());

        //mainPanel.add(createMessagePanel(), BorderLayout.CENTER);
        mainPanel.add(messageTabbedPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMessagePanel() {
        message = new JLabeledTextArea("Message:");
        message.setName(AzStorageQueueSampler.MESSAGE);

        messageFile = new JLabeledTextField("Message filename:");
        messageFile.setName(AzStorageQueueSampler.MESSAGE_FILE);

        JPanel messagePanel = new JPanel(new BorderLayout(0, 5));
        messagePanel.add(messageFile, BorderLayout.NORTH);
        messagePanel.add(message, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(createMessageTypePanel(), BorderLayout.NORTH);
        panel.add(messagePanel, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createMessageAdvancedPanel() {
        visibilityTimeout = new JLabeledTextField("Visibility timeout (sec):");
        visibilityTimeout.setName(AzStorageQueueSampler.VISIBILITY_TIMEOUT);

        timeToLive = new JLabeledTextField("Time to live (sec):");
        timeToLive.setName(AzStorageQueueSampler.TIME_TO_LIVE);

        timeout = new JLabeledTextField("Timeout (sec):");
        timeout.setName(AzStorageQueueSampler.TIMEOUT);

        VerticalPanel panel = new VerticalPanel();
        panel.add(visibilityTimeout);
        panel.add(timeToLive);
        panel.add(timeout);
        return panel;
    }

    private JPanel createMessageTypePanel() {
        VerticalPanel messageTypePanel = new VerticalPanel();
        JLabel messageTypeLabel = new JLabel("Message type:");

        messageType = new JLabeledChoice("", MESSAGE_TYPE_LABELS);
        messageType.setName(AzStorageQueueSampler.MESSAGE_TYPE);
        messageType.addChangeListener(this);

        messageTypePanel.add(messageTypeLabel, BorderLayout.WEST);
        messageTypePanel.add(messageType, BorderLayout.CENTER);

        return messageTypePanel;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(messageType)) {
            toggleMessageType();
        }
    }

    private void toggleMessageType() {
        String type = messageType.getText();
        if (type.equals(AzStorageQueueSampler.MESSAGE_TYPE_FILE)) {
            message.setVisible(false);
            messageFile.setVisible(true);
        } else { // MESSAGE_TYPE_STRING
            message.setVisible(true);
            messageFile.setVisible(false);
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
