package jp.co.pnop.jmeter.protocol.azureeventhubs.sampler.gui;

import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import javax.swing.JLabel;

import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessage;
import jp.co.pnop.jmeter.protocol.amqp.sampler.gui.AzAmqpMessagesPanel;

public class AzEventHubsMessagesPanel extends AzAmqpMessagesPanel {

    public AzEventHubsMessagesPanel() {
        super("Messages");
    }

    public AzEventHubsMessagesPanel(String label) {
        tableLabel = new JLabel(label);
        init();
    }

    /**
     * Initialize the table model used for the messages table.
     */
    @Override
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(
            /*
            new String[] { COLUMN_NAMES.get("MESSAGE_TYPE"), COLUMN_NAMES.get("MESSAGE"), COLUMN_NAMES.get("MESSAGE_ID"), "partition key", COLUMN_NAMES.get("CONTENT_TYPE"), COLUMN_NAMES.get("LABEL"), COLUMN_NAMES.get("STANDARD_PROPERTIES"), COLUMN_NAMES.get("CUSTOM_PROPERTIES") },
            AzAmqpMessage.class,
            new Functor[] { new Functor("getMessageType"), new Functor("getMessage"), new Functor("getMessageId"), new Functor("getPartitionKey"), new Functor("getContentType"), new Functor("getLabel"), new Functor("getStandardProperties"), new Functor("getCustomProperties") },
            new Functor[] { new Functor("setMessageType"), new Functor("setMessage"), new Functor("setMessageId"), new Functor("setPartitionKey"), new Functor("setContentType"), new Functor("setLabel"), new Functor("setStandardProperties"), new Functor("setCustomProperties") },
            new Class[] { String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class }
            */
            new String[] { COLUMN_NAMES.get("MESSAGE_TYPE"), COLUMN_NAMES.get("MESSAGE"), COLUMN_NAMES.get("MESSAGE_ID"), COLUMN_NAMES.get("CONTENT_TYPE"), COLUMN_NAMES.get("CUSTOM_PROPERTIES") },
            AzAmqpMessage.class,
            new Functor[] { new Functor("getMessageType"), new Functor("getMessage"), new Functor("getMessageId"), new Functor("getContentType"), new Functor("getCustomProperties") },
            new Functor[] { new Functor("setMessageType"), new Functor("setMessage"), new Functor("setMessageId"), new Functor("setContentType"), new Functor("setCustomProperties") },
            new Class[] { String.class, String.class, String.class, String.class, String.class }
        );
    }

}
