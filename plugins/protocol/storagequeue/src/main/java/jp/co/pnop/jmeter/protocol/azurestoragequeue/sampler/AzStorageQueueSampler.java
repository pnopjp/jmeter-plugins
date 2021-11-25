package jp.co.pnop.jmeter.protocol.azurestoragequeue.sampler;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

import com.azure.core.http.rest.Response;
import com.azure.storage.queue.*;
import com.azure.storage.queue.models.QueueStorageException;
import com.azure.storage.queue.models.SendMessageResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azurestoragequeue.common.AzStorageConnectionParams;

public class AzStorageQueueSampler extends AbstractSampler implements TestStateListener {
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzStorageQueueSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
        Arrays.asList(
            "org.apache.jmeter.config.gui.SimpleConfigGui"
        )
    );

    public static final String MESSAGE_TYPE = "messageType";
    public static final String MESSAGE = "message";
    public static final String MESSAGE_FILE = "messageFile";
    public static final String VISIBILITY_TIMEOUT = "visibilityTimeout";
    public static final String TIME_TO_LIVE = "timeToLive";
    public static final String TIMEOUT = "timeout";
    
    public static final String MESSAGE_TYPE_STRING = "String / Base64 encoded binary";
    public static final String MESSAGE_TYPE_FILE = "File";

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzStorageQueueSampler() {
        super();
        classCount.incrementAndGet();
        trace("AzStorageQueueSamler()");
    }

    /**
     * Clear the messages.
     */
    @Override
    public void clear() {
        super.clear();
        
        setProperty(new TestElementProperty(AzStorageConnectionParams.STORAGE_CONNECTION_PARAMS, null));
        setProperty(new StringProperty(MESSAGE_TYPE, MESSAGE_TYPE_STRING));
        setProperty(new StringProperty(MESSAGE, ""));
        setProperty(new StringProperty(MESSAGE_FILE, ""));
        setProperty(new StringProperty(VISIBILITY_TIMEOUT, ""));
        setProperty(new StringProperty(TIME_TO_LIVE, ""));
        setProperty(new StringProperty(TIMEOUT, ""));
    }

    public void setStorageConnectionParams(AzStorageConnectionParams connectionParams) {
        setProperty(new TestElementProperty(AzStorageConnectionParams.STORAGE_CONNECTION_PARAMS, connectionParams));
    }

    public AzStorageConnectionParams getStorageConnectionParams() {
        return (AzStorageConnectionParams) getProperty(AzStorageConnectionParams.STORAGE_CONNECTION_PARAMS).getObjectValue();
    }

    public void setMessageType(String messageType) {
        setProperty(new StringProperty(MESSAGE_TYPE, messageType));
    }

    public String getMessageType() {
        return getPropertyAsString(MESSAGE_TYPE);
    }

    public void setMessage(String message) {
        setProperty(new StringProperty(MESSAGE, message));
    }

    public String getMessage() {
        return getPropertyAsString(MESSAGE);
    }

    public void setMessageFile(String messageFile) {
        setProperty(new StringProperty(MESSAGE_FILE, messageFile));
    }

    public String getMessageFile() {
        return getPropertyAsString(MESSAGE_FILE);
    }

    public void setVisibilityTimeout(String visibilityTimeout) {
        setProperty(new StringProperty(VISIBILITY_TIMEOUT, visibilityTimeout));
    }

    public String getVisibilityTimeout() {
        return getPropertyAsString(VISIBILITY_TIMEOUT);
    }

    public void setTimeToLive(String timeToLive) {
        setProperty(new StringProperty(TIME_TO_LIVE, timeToLive));
    }

    public String getTimeToLive() {
        return getPropertyAsString(TIME_TO_LIVE);
    }

    public void setTimeout(String timeout) {
        setProperty(new StringProperty(TIMEOUT, timeout));
    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        trace("sample()");
        boolean isSuccessful = false;

        SampleResult res = new SampleResult();
        res.setSampleLabel(this.getName());

        String threadName = Thread.currentThread().getName();
        String responseData = "";
        String responseMessage = "";
        String requestBody = "";
        String requestHeaders = "";
        long bodyBytes = 0;
        long bytes = 0;
        long sentBytes = 0;

        QueueClient connection = null;
        AzStorageConnectionParams connectionParams = getStorageConnectionParams();

        try {
            res.sampleStart(); // Start timing

            Duration visibilityTimeout = null;
            Duration timeToLive = null;
            Duration timeout = null;
            if (!getVisibilityTimeout().isBlank()) {
                try {
                    visibilityTimeout = Duration.ofSeconds(Long.parseLong(getVisibilityTimeout()));
                } catch (NumberFormatException exc) {
                    throw new NumberFormatException(exc.getMessage().concat(" [Visibility timeout]"));
                }
            }
            if (!getTimeToLive().isBlank()) {
                try {
                    timeToLive = Duration.ofSeconds(Long.parseLong(getTimeToLive()));
                } catch (NumberFormatException exc) {
                    throw new NumberFormatException(exc.getMessage().concat(" [Time to live]"));
                }
            }
            if (!getTimeout().isBlank()) {
                try {
                    timeout = Duration.ofSeconds(Long.parseLong(getTimeout()));
                } catch (NumberFormatException exc) {
                    throw new NumberFormatException(exc.getMessage().concat(" [Timeout]"));
                }
            }

            String message = "";
            if (getMessageType().equals(MESSAGE_TYPE_FILE)) {
                byte[] file = FileUtils.readFileToByteArray(new File(getMessageFile()));
                message = Base64.getEncoder().encodeToString(file);
                requestBody = "Filename: ".concat(getMessageFile());
            } else { // MESSAGE_TYPE_STRING
                message = getMessage();
                requestBody = message;
            }
            requestHeaders = connectionParams.getMaskedParams();

            connection = connectionParams.getConnection();
            Response<SendMessageResult> response = connection.sendMessageWithResponse(message, visibilityTimeout, timeToLive, timeout, null);
            
            res.setResponseCode(String.valueOf(response.getStatusCode()));
            responseData = "Message Id: ".concat(response.getValue().getMessageId()).concat("\n")
                         .concat("Pop Receipt: ").concat(response.getValue().getPopReceipt());
            
            res.latencyEnd();
            responseMessage = response.getValue().getMessageId().toString();
            isSuccessful = true;
            res.sampleEnd();
        } catch (QueueStorageException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            responseData = ex.getMessage();
            responseMessage = responseMessage.concat(responseData);
        } catch (IllegalStateException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            responseData = ex.getMessage();
            responseMessage = responseMessage.concat(responseData);
        } catch (IllegalArgumentException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            responseData = ex.getMessage();
            responseMessage = responseMessage.concat(responseData);
        } catch (FileNotFoundException ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            responseData = ex.getMessage();
            responseMessage = responseMessage.concat(responseData);
        } catch (Exception ex) {
            log.info("Error calling {} sampler. ", threadName, ex);
            
            try {
                String typeName = ex.getCause().getClass().getTypeName();
                if (typeName.equals("java.net.UnknownHostException")
                || typeName.equals("io.netty.channel.AbstractChannel$AnnotatedConnectException")) {
                    responseData = ex.getCause().getMessage();
                } else {
                    responseData = ex.toString();
                }
            } catch (Exception exc) {
                responseData = ex.toString();
            }
            responseMessage = responseMessage.concat(responseData);
        } finally {
            res.setDataType(SampleResult.TEXT);
            res.setResponseData(responseData, "UTF-8");
            res.setSamplerData(requestBody); // Request Body
            res.setRequestHeaders(requestHeaders);
            res.setBytes(bytes);
            res.setBodySize(bodyBytes);
            res.setSentBytes(sentBytes);
            res.setResponseMessage(responseMessage);
        }

        res.setSuccessful(isSuccessful);
        return res;
    }

    @Override
    public void testStarted() {
        testStarted(""); // $NON-NLS-1$
    }

    @Override
    public void testEnded() {
        testEnded(""); // $NON-NLS-1$
    }

    @Override
    public void testStarted(String host) {
        // ignored
    }

    // Ensure any remaining contexts are closed
    @Override
    public void testEnded(String host) {

    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
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
