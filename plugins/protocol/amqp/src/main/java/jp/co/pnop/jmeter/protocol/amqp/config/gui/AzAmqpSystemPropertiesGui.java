package jp.co.pnop.jmeter.protocol.amqp.config.gui;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;

//import org.apache.jorphan.gui.JLabeledTextField;

import jp.co.pnop.jmeter.protocol.amqp.config.AzAmqpSystemProperties;

public class AzAmqpSystemPropertiesGui extends AbstractConfigGui {
    private static final long serialVersionUID = 1L;

    //private JLabeledTextField messageId;
    private JTextField messageId;
    private JTextField userId;
    private JTextField to;
    private JTextField subject;
    private JTextField replyTo;
    private JTextField correlationId;
    private JTextField contentType;
    private JTextField contentEncoding;
    private JTextField absoluteExpiryTime;
    private JTextField creationTime;
    private JTextField groupId;
    private JTextField groupSequence;
    private JTextField replyToGroupId;

    public AzAmqpSystemPropertiesGui() {
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        messageId.setText(element.getPropertyAsString(AzAmqpSystemProperties.MESSAGE_ID));
        userId.setText(element.getPropertyAsString(AzAmqpSystemProperties.USER_ID));
        to.setText(element.getPropertyAsString(AzAmqpSystemProperties.TO));
        subject.setText(element.getPropertyAsString(AzAmqpSystemProperties.SUBJECT));
        replyTo.setText(element.getPropertyAsString(AzAmqpSystemProperties.REPLY_TO));
        correlationId.setText(element.getPropertyAsString(AzAmqpSystemProperties.CORRELATION_ID));
        contentType.setText(element.getPropertyAsString(AzAmqpSystemProperties.CONTENT_TYPE));
        contentEncoding.setText(element.getPropertyAsString(AzAmqpSystemProperties.CONTENT_ENCODING));
        absoluteExpiryTime.setText(element.getPropertyAsString(AzAmqpSystemProperties.ABSOLUTE_EXPIRY_TIME));
        creationTime.setText(element.getPropertyAsString(AzAmqpSystemProperties.CREATION_TIME));
        groupId.setText(element.getPropertyAsString(AzAmqpSystemProperties.GROUP_ID));
        groupSequence.setText(element.getPropertyAsString(AzAmqpSystemProperties.GROUP_SEQUENCE));
        replyToGroupId.setText(element.getPropertyAsString(AzAmqpSystemProperties.REPLY_TO_GROUP_ID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "AMQP System Properties";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement te) {
        te.clear();
        configureTestElement(te);
        te.setProperty(AzAmqpSystemProperties.MESSAGE_ID, messageId.getText());
        te.setProperty(AzAmqpSystemProperties.USER_ID, userId.getText());
        te.setProperty(AzAmqpSystemProperties.TO, to.getText());
        te.setProperty(AzAmqpSystemProperties.SUBJECT, subject.getText());
        te.setProperty(AzAmqpSystemProperties.REPLY_TO, replyTo.getText());
        te.setProperty(AzAmqpSystemProperties.CORRELATION_ID, correlationId.getText());
        te.setProperty(AzAmqpSystemProperties.CONTENT_TYPE, contentType.getText());
        te.setProperty(AzAmqpSystemProperties.CONTENT_ENCODING, contentEncoding.getText());
        te.setProperty(AzAmqpSystemProperties.ABSOLUTE_EXPIRY_TIME, absoluteExpiryTime.getText());
        te.setProperty(AzAmqpSystemProperties.CREATION_TIME, creationTime.getText());
        te.setProperty(AzAmqpSystemProperties.GROUP_ID, groupId.getText());
        te.setProperty(AzAmqpSystemProperties.GROUP_SEQUENCE, groupSequence.getText());
        te.setProperty(AzAmqpSystemProperties.REPLY_TO_GROUP_ID, replyToGroupId.getText());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        AzAmqpSystemProperties config = new AzAmqpSystemProperties();
        modifyTestElement(config);
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui(){
        super.clearGui();
        messageId.setText("");
        userId.setText("");
        to.setText("");
        subject.setText("");
        replyTo.setText("");
        correlationId.setText("");
        contentType.setText("");
        contentEncoding.setText("");
        absoluteExpiryTime.setText("");
        creationTime.setText("");
        groupId.setText("");
        groupSequence.setText("");
        replyToGroupId.setText("");
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        //JPanel p = this;

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        //p = new JPanel();

        // Specific setup
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createPropertiesPanel());        

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createPropertiesPanel() {

        JPanel panel = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        GridBagConstraints labelConst = new GridBagConstraints();
        GridBagConstraints valueConst = new GridBagConstraints();
        labelConst.gridx = 0;
        labelConst.anchor = GridBagConstraints.WEST;
        labelConst.insets = new Insets(0, 0, 0, 5);
        valueConst.weightx = 1;
        valueConst.gridx = 1;
        valueConst.fill = GridBagConstraints.HORIZONTAL;
        int row = -1;
        
        panel.setBorder(BorderFactory.createTitledBorder("System Properties"));

        JLabel messageIdLabel = new JLabel("message-id:");
        messageId = new JTextField();
        messageId.setName(AzAmqpSystemProperties.MESSAGE_ID);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(messageIdLabel, labelConst);
        layout.setConstraints(messageId, valueConst);
        panel.add(messageIdLabel);
        panel.add(messageId);

        JLabel userIdLabel = new JLabel("user-id:");
        userId = new JTextField();
        userId.setName(AzAmqpSystemProperties.USER_ID);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(userIdLabel, labelConst);
        layout.setConstraints(userId, valueConst);
        panel.add(userIdLabel);
        panel.add(userId);

        JLabel toLabel = new JLabel("to:");
        to = new JTextField();
        to.setName(AzAmqpSystemProperties.TO);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(toLabel, labelConst);
        layout.setConstraints(to, valueConst);
        panel.add(toLabel);
        panel.add(to);

        JLabel subjectLabel = new JLabel("subject:");
        subject = new JTextField();
        subject.setName(AzAmqpSystemProperties.SUBJECT);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(subjectLabel, labelConst);
        layout.setConstraints(subject, valueConst);
        panel.add(subjectLabel);
        panel.add(subject);

        JLabel replyToLabel = new JLabel("reply-to:");
        replyTo = new JTextField();
        replyTo.setName(AzAmqpSystemProperties.REPLY_TO);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(replyToLabel, labelConst);
        layout.setConstraints(replyTo, valueConst);
        panel.add(replyToLabel);
        panel.add(replyTo);

        JLabel correlationIdLabel = new JLabel("correlation-id:");
        correlationId = new JTextField();
        correlationId.setName(AzAmqpSystemProperties.CORRELATION_ID);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(correlationIdLabel, labelConst);
        layout.setConstraints(correlationId, valueConst);
        panel.add(correlationIdLabel);
        panel.add(correlationId);

        JLabel contentTypeLabel = new JLabel("content-type:");
        contentType = new JTextField();
        contentType.setName(AzAmqpSystemProperties.CONTENT_TYPE);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(contentTypeLabel, labelConst);
        layout.setConstraints(contentType, valueConst);
        panel.add(contentTypeLabel);
        panel.add(contentType);

        JLabel contentEncodingLabel = new JLabel("content-encoding:");
        contentEncoding = new JTextField();
        contentEncoding.setName(AzAmqpSystemProperties.CONTENT_ENCODING);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(contentEncodingLabel, labelConst);
        layout.setConstraints(contentEncoding, valueConst);
        panel.add(contentEncodingLabel);
        panel.add(contentEncoding);

        JLabel absoluteExpiryTimeLabel = new JLabel("absolute-expiry-time:");
        absoluteExpiryTime = new JTextField();
        absoluteExpiryTime.setName(AzAmqpSystemProperties.ABSOLUTE_EXPIRY_TIME);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(absoluteExpiryTimeLabel, labelConst);
        layout.setConstraints(absoluteExpiryTime, valueConst);
        panel.add(absoluteExpiryTimeLabel);
        panel.add(absoluteExpiryTime);

        JLabel creationTimeLabel = new JLabel("creation-time:");
        creationTime = new JTextField();
        creationTime.setName(AzAmqpSystemProperties.CREATION_TIME);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(creationTimeLabel, labelConst);
        layout.setConstraints(creationTime, valueConst);
        panel.add(creationTimeLabel);
        panel.add(creationTime);

        JLabel groupIdLabel = new JLabel("group-id:");
        groupId = new JTextField();
        groupId.setName(AzAmqpSystemProperties.GROUP_ID);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(groupIdLabel, labelConst);
        layout.setConstraints(groupId, valueConst);
        panel.add(groupIdLabel);
        panel.add(groupId);

        JLabel groupSequenceLabel = new JLabel("group-sequence:");
        groupSequence = new JTextField();
        groupSequence.setName(AzAmqpSystemProperties.GROUP_SEQUENCE);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(groupSequenceLabel, labelConst);
        layout.setConstraints(groupSequence, valueConst);
        panel.add(groupSequenceLabel);
        panel.add(groupSequence);

        JLabel replyToGroupIdLabel = new JLabel("reply-to-group-id:");
        replyToGroupId = new JTextField();
        replyToGroupId.setName(AzAmqpSystemProperties.REPLY_TO_GROUP_ID);
        row ++;
        labelConst.gridy = row;
        valueConst.gridy = row;
        layout.setConstraints(replyToGroupIdLabel, labelConst);
        layout.setConstraints(replyToGroupId, valueConst);
        panel.add(replyToGroupIdLabel);
        panel.add(replyToGroupId);

        return panel;
    }

}
