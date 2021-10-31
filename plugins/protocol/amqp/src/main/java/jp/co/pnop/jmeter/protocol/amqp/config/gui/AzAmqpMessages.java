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

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class AzAmqpMessages extends ConfigTestElement {
    
    private static final long serialVersionUID = 1L;
    //private static final Logger log = LoggerFactory.getLogger(AzAmqpMessages.class);

    public static final String MESSAGES = "Messages.messages"; //$NON-NLS$

    public static final String MESSAGE_TYPE_STRING = "String";
    public static final String MESSAGE_TYPE_BASE64 = "Base64 encoded binary";
    public static final String MESSAGE_TYPE_FILE = "File";

    /**
     * Create a new Messages object with no messages.
     */
    public AzAmqpMessages() {
        setProperty(new CollectionProperty(MESSAGES, new ArrayList<>()));
    }

    /**
     * Get the messages.
     *
     * @return the messages
     */
    public CollectionProperty getMessages() {
        return (CollectionProperty) getProperty(MESSAGES);
    }

    /**
     * Clear the messages.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(MESSAGES, new ArrayList<>()));
    }

    /**
     * Set the list of messages. Any existing messages will be lost.
     *
     * @param messages
     *            the new messages
     */
    public void setMessages(List<Object> messages) {
        setProperty(new CollectionProperty(MESSAGES, messages));
    }

    /**
     * Add a new message.
     *
     * @param msg
     *            the new message
     */
    public void addMessage(AzAmqpMessage msg) {
        TestElementProperty newMsg = new TestElementProperty(msg.getMessage(), msg);
        if (isRunningVersion()) {
            this.setTemporary(newMsg);
        }
        getMessages().addItem(newMsg);
    }

    /**
     * Get a PropertyIterator of the messages.
     *
     * @return an iteration of the messages
     */
    public PropertyIterator iterator() {
        return getMessages().iterator();
    }

    /**
     * Remove the specified message from the list.
     *
     * @param row
     *            the index of the message to remove
     */
    public void removeMessage(int row) {
        if (row < getMessages().size()) {
            getMessages().remove(row);
        }
    }

    /**
     * Remove the specified message from the list.
     *
     * @param msg
     *            the message to remove
     */
    public void removeMessage(AzAmqpMessage msg) {
        PropertyIterator iter = getMessages().iterator();
        while (iter.hasNext()) {
            AzAmqpMessage item = (AzAmqpMessage) iter.next().getObjectValue();
            if (msg.equals(item)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove the message with the specified name.
     *
     * @param msgName
     *            the name of the message to remove
     */
    public void removeMessage(String msgName) {
        PropertyIterator iter = getMessages().iterator();
        while (iter.hasNext()) {
            AzAmqpMessage msg = (AzAmqpMessage) iter.next().getObjectValue();
            if (msg.getName().equals(msgName)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove all messages from the list.
     */
    public void removeAllMessages() {
        getMessages().clear();
    }

    /**
     * Get the number of messages in the list.
     *
     * @return the number of messages
     */
    public int getMessageCount() {
        return getMessages().size();
    }

    /**
     * Get a single message.
     *
     * @param row
     *            the index of the message to return.
     * @return the message at the specified index, or null if no message
     *         exists at that index.
     */
    public AzAmqpMessage getMessage(int row) {
        AzAmqpMessage message = null;

        if (row < getMessages().size()) {
            message = (AzAmqpMessage) getMessages().get(row).getObjectValue();
        }

        return message;
    }

}
