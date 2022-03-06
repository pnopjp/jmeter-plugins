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

package jp.co.pnop.jmeter.protocol.amqp.sampler;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class AzAmqpMessage extends AbstractTestElement {

    private static final long serialVersionUID = 1L;
    //private static final Logger log = LoggerFactory.getLogger(AzAmqpMessage.class);

    private static final String MESSAGE_TYPE = "Message.messageType"; //$NON-NLS$
    private static final String MESSAGE = "Message.message"; //$NON-NLS$
    private static final String MESSAGE_ID = "Message.messageId"; //$NON-NLS$
    private static final String GROUP_ID = "Message.groupId"; //$NON-NLS$
    private static final String PARTITION_KEY = "Message.partitionKey"; //$NON-NLS$
    private static final String CUSTOM_PROPERTIES = "Message.customProperties"; //$NON-NLS$

    public AzAmqpMessage() {
    }

    /**
     * Create a new Message with the specified system properties and custom properties
     *
     * @param messageType
     *            type of message
     */

    public AzAmqpMessage(String messageType) {
        setProperty(new StringProperty(MESSAGE_TYPE, messageType));
        setProperty(new StringProperty(MESSAGE, ""));
        setProperty(new StringProperty(PARTITION_KEY, ""));
        setProperty(new StringProperty(MESSAGE_ID, ""));
        setProperty(new StringProperty(GROUP_ID, ""));
        setProperty(new StringProperty(CUSTOM_PROPERTIES, ""));
    }

    /**
     * Set the message type of the Message.
     *
     * @param newMessageType
     *            the new message type
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
     * Set the message Id of the Message.
     *
     * @param newMessageId
     *            the new message Id
     */
    public void setMessageId(String newMessageId) {
        setProperty(new StringProperty(MESSAGE_ID, newMessageId));
    }

    /**
     * Get the messageId.
     *
     * @return the message Id
     */
    public String getMessageId() {
        return getPropertyAsString(MESSAGE_ID);
    }

    /**
     * Set the group Id of the Message.
     *
     * @param newGroupId
     *            the new group Id
     */
    public void setGroupId(String newGroupId) {
        setProperty(new StringProperty(GROUP_ID, newGroupId));
    }

    /**
     * Get the groupId.
     *
     * @return the group Id
     */
    public String getGroupId() {
        return getPropertyAsString(GROUP_ID);
    }

    /**
     * Set the partition key of the Message.
     *
     * @param newPartitionKey
     *            the new message Id
     */
    public void setPartitionKey(String newPartitionKey) {
        setProperty(new StringProperty(PARTITION_KEY, newPartitionKey));
    }

    /**
     * Get the partitionKey.
     *
     * @return the partition key
     */
    public String getPartitionKey() {
        return getPropertyAsString(PARTITION_KEY);
    }

    /**
     * Set the custom properties of the Message.
     *
     * @param newCustomProperties
     *            the new custom properties
     */
    public void setCustomProperties(String newCustomProperties) {
        setProperty(new StringProperty(CUSTOM_PROPERTIES, newCustomProperties));
    }

    /**
     * Get the custom properties.
     *
     * @return the custom properties
     */
    public String getCustomProperties() {
        return getPropertyAsString(CUSTOM_PROPERTIES);
    }

}
