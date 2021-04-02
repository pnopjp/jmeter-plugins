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

package jp.co.pnop.jmeter.protocol.amqp.config.gui;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzAmqpMessage extends AbstractTestElement {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzAmqpMessage.class);

    private static final String MESSAGE_TYPE = "Message.messageType"; //$NON-NLS$
    private static final String MESSAGE = "Message.message"; //$NON-NLS$
    private static final String SYSTEM_PROPERTIES = "Message.systemProperties"; //$NON-NLS$
    private static final String APP_PROPERTIES = "Message.appProperties"; //$NON-NLS$

    public AzAmqpMessage() {
    }

    /**
     * Create a new Message with the specified system properties and application properties
     *
     * @param messageType
     *            type of message
     * @param message
     *            AMQP message
     * @param systemProperties
     *            AMQP system properties
     * @param appProperties
     *            AMQP application properties
     */
    public AzAmqpMessage(String messageType, String message, Object systemProperties, Object appProperties) {
        setProperty(new StringProperty(MESSAGE_TYPE, messageType));
        setProperty(new StringProperty(MESSAGE, message));
        setProperty(new ObjectProperty(SYSTEM_PROPERTIES, systemProperties));
        setProperty(new ObjectProperty(APP_PROPERTIES, appProperties));
    }

    /**
     * Set the message type of the Message.
     *
     * @param newMessageType
     *            the new message
     */
    public void setMessageType(String newMessageType) {
        setProperty(new StringProperty(MESSAGE_TYPE, newMessageType));
    }

    /**
     * Get the message type.
     *
     * @return the message type
     */
    public String getMessageType() {
        return getPropertyAsString(MESSAGE_TYPE);
    }

    /**
     * Set the message of the Message.
     *
     * @param newMessage
     *            the new message
     */
    public void setMessage(String newMessage) {
        setProperty(new StringProperty(MESSAGE, newMessage));
    }

    /**
     * Get the message.
     *
     * @return the message
     */
    public String getMessage() {
        return getPropertyAsString(MESSAGE);
    }

    /**
     * Sets the systemProperties of the Message.
     *
     * @param newSystemProperties
     *            the new system properties
     */
    public void setSystemProperties(Object newSystemProperties) {
        setProperty(new ObjectProperty(SYSTEM_PROPERTIES, newSystemProperties));
    }

    /**
     * Gets the system properties of Message object.
     *
     * @return the system properties's value
     */
    public Object getSystemProperties() {
        return getPropertyAsString(SYSTEM_PROPERTIES);
    }

    /**
     * Sets the application properties of the Messages.
     *
     * @param newAppProperties
     *            the new application properties
     */
    public void setAppProperties(Object newAppProperties) {
        setProperty(new ObjectProperty(APP_PROPERTIES, newAppProperties));
    }

    /**
     * Gets the opcode of the properties object.
     *
     * @return the properties's value
     */
    public Object getAppProperties() {
        return getPropertyAsString(APP_PROPERTIES);
    }

}
