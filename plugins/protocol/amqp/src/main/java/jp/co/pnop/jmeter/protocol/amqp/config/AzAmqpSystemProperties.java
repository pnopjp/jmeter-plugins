package jp.co.pnop.jmeter.protocol.amqp.config;

import org.apache.jmeter.config.ConfigTestElement;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzAmqpSystemProperties extends ConfigTestElement {

    private static final long serialVersionUID = 1L;

    public static final String MESSAGE_ID = "AzAmqpSystemProperties.messageId"; //$NON-NLS-1$
    public static final String USER_ID = "AzAmqpSystemProperties.userId"; //$NON-NLS-1$
    public static final String TO = "AzAmqpSystemProperties.to"; //$NON-NLS-1$
    public static final String SUBJECT = "AzAmqpSystemProperties.subject"; //$NON-NLS-1$
    public static final String REPLY_TO = "AzAmqpSystemProperties.replyTo"; //$NON-NLS-1$
    public static final String CORRELATION_ID = "AzAmqpSystemProperties.correlationId"; //$NON-NLS-1$
    public static final String CONTENT_TYPE = "AzAmqpSystemProperties.contentType"; //$NON-NLS-1$
    public static final String CONTENT_ENCODING = "AzAmqpSystemProperties.contentEncoding"; //$NON-NLS-1$
    public static final String ABSOLUTE_EXPIRY_TIME = "AzAmqpSystemProperties.absoluteExpiryTime"; //$NON-NLS-1$
    public static final String CREATION_TIME = "AzAmqpSystemProperties.creationTime"; //$NON-NLS-1$
    public static final String GROUP_ID = "AzAmqpSystemProperties.groupId"; //$NON-NLS-1$
    public static final String GROUP_SEQUENCE = "AzAmqpSystemProperties.groupSequence"; //$NON-NLS-1$
    public static final String REPLY_TO_GROUP_ID = "AzAmqpSystemProperties.replyToGroupId"; //$NON-NLS-1$
    
    private static final Logger log = LoggerFactory.getLogger(AzAmqpSystemProperties.class);

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzAmqpSystemProperties() {
        classCount.incrementAndGet();
        trace("AzAmqpSystemProperties()");
    }

    /**
     * @return a string for the sampleResult Title
     */
    private String getTitle() {
        return this.getName();
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

    /*
    public HashMap<String, Object> getProperties() {
        
    }
    */
}
