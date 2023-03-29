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

package jp.co.pnop.jmeter.protocol.amqp.sampler.gui;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessage;
import jp.co.pnop.jmeter.protocol.amqp.sampler.AzAmqpMessages;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class AzAmqpMessagesPanel extends AbstractSamplerGui implements ActionListener {
    
    private static final long serialVersionUID = 1L;
    //private static final Logger log = LoggerFactory.getLogger(AzAmqpMessagesPanel.class);

    /** The title label for this component. */
    protected JLabel tableLabel;

    /** The table containing the list of messages. */
    private transient JTable table;

    /** The model for the messages table. */
    // needs to be accessible from test code
    protected transient ObjectTableModel tableModel; // Only contains AzAmqpMessage entries

    /** A button for removing messages from the table. */
    private JButton delete;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; //$NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; //$NON-NLS-1$

    protected static Map<String, String> COLUMN_NAMES = new HashMap<>();
    static {
        COLUMN_NAMES.put("MESSAGE_TYPE", "message type"); //$NON-NLS-1$
        COLUMN_NAMES.put("MESSAGE", "message"); //$NON-NLS-1$
        COLUMN_NAMES.put("MESSAGE_ID", "message Id"); //$NON-NLS-1$
        COLUMN_NAMES.put("GROUP_ID", "group Id"); //$NON-NLS-1$
        COLUMN_NAMES.put("CUSTOM_PROPERTIES", "custom properties"); //$NON-NLS-1$
        COLUMN_NAMES.put("CONTENT_TYPE", "content type"); //$NON-NLS-1$
        COLUMN_NAMES.put("LABEL", "label/subject"); //$NON-NLS-1$
        COLUMN_NAMES.put("STANDARD_PROPERTIES", "headers/properties/annotations"); //$NON-NLS-1$
    }

    public AzAmqpMessagesPanel() {
        this("Event data"); //$NON-NLS-1$
    }

    public AzAmqpMessagesPanel(String label) {
        tableLabel = new JLabel(label);
        init();
    }
    
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    @Override
    public String getLabelResource() {
        return "Azure Event Hubs Default"; // $NON-NLS-1$
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        AzAmqpMessages msgs = new AzAmqpMessages();
        modifyTestElement(msgs);
        return (TestElement) msgs.clone();
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement msgs) {
        GuiUtils.stopTableEditing(table);
        if (msgs instanceof AzAmqpMessages) {
            AzAmqpMessages messages = (AzAmqpMessages) msgs;
            messages.clear();
            @SuppressWarnings("unchecked") // Only contains AzAmqpMessage entries
            Iterator<AzAmqpMessage> modelData = (Iterator<AzAmqpMessage>) tableModel.iterator();
            while (modelData.hasNext()) {
                AzAmqpMessage msg = modelData.next();
                messages.addMessage(msg);
            }
        }
        super.configureTestElement(msgs);
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param el
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof AzAmqpMessages) {
            tableModel.clearData();
            PropertyIterator iter = ((AzAmqpMessages) el).iterator();
            while (iter.hasNext()) {
                AzAmqpMessage msg = (AzAmqpMessage) iter.next().getObjectValue();
                tableModel.addRow(msg);
            }
        }
        checkDeleteStatus();
    }
    
    /**
     * Enable or disable the delete button depending on whether or not there is
     * a row to be deleted.
     */
    private void checkDeleteStatus() {
        // Disable DELETE if there are no rows in the table to delete.
        if (tableModel.getRowCount() == 0) {
            delete.setEnabled(false);
        } else {
            delete.setEnabled(true);
        }
    }
    
    /**
     * Clear all rows from the table.
     */
    public void clear() {
        tableModel.clearData();
    }

    /**
     * Invoked when an action occurs. This implementation supports the add and
     * delete buttons.
     *
     * @param e
     *            the event that has occurred
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(DELETE)) {
            deleteMessage();
        } else if (action.equals(ADD)) {
            addMessage();
        }
    }
    
    /**
     * Remove the currently selected message from the table.
     */
    private void deleteMessage() {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        GuiUtils.cancelEditing(table);

        int rowSelected = table.getSelectedRow();
        if (rowSelected >= 0) {
            tableModel.removeRow(rowSelected);
            tableModel.fireTableDataChanged();

            // Disable DELETE if there are no rows in the table to delete.
            if (tableModel.getRowCount() == 0) {
                delete.setEnabled(false);
            }

            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            else {
                int rowToSelect = rowSelected;

                if (rowSelected >= tableModel.getRowCount()) {
                    rowToSelect = rowSelected - 1;
                }

                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }
    
    /**
     * Add a new message row to the table.
     */
    private void addMessage() {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addRow(makeNewAzAmqpMessage());

        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);

        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }
    
    /**
     * Create a new AzAmqpMessage object.
     *
     * @return a new AzAmqpMessage object
     */
    private AzAmqpMessage makeNewAzAmqpMessage() {
        //return new AzAmqpMessage("String", "", "", "", "");
        return new AzAmqpMessage("String");
    }
    

    /**
     * Initialize the table model used for the messages table.
     */
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(
            new String[] { COLUMN_NAMES.get("MESSAGE_TYPE"), COLUMN_NAMES.get("MESSAGE") },
            AzAmqpMessage.class,
            new Functor[] { new Functor("getMessageType"), new Functor("getMessage") },
            new Functor[] { new Functor("setMessageType"), new Functor("setMessage") },
            new Class[] { String.class, String.class }
        );
    }
    
    public static boolean testFunctors(){
        AzAmqpMessagesPanel instance = new AzAmqpMessagesPanel();
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    /**
     * Create the main GUI panel which contains the message table.
     *
     * @return the main GUI panel
     */
    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        JMeterUtils.applyHiDPI(table);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn messageTypeColumn = table.getColumnModel().getColumn(0);
        messageTypeColumn.setCellEditor(new MessageTypeCelEditor());

        tableModel.addRow(new AzAmqpMessage(AzAmqpMessages.MESSAGE_TYPE_BASE64));
        resizeColumnWidth(table, 0);
        tableModel.clearData();
        //hideColumn(table, 2);
        
        return makeScrollPane(table);
    }

    private void resizeColumnWidth(JTable table, int column) {
        final TableColumnModel columnModel = table.getColumnModel();
        int width = 10;
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component comp = table.prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width + 1 , width);
        }
        columnModel.getColumn(column).setMaxWidth(width);
        columnModel.getColumn(column).setMinWidth(width);
        columnModel.getColumn(column).setResizable(false);
    }

    /*
    private void hideColumn(JTable table, int column) {
        final TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setMinWidth(0);
        columnModel.getColumn(column).setMaxWidth(0);
    }
    */
    
    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    private Component makeLabelPanel() {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(tableLabel);
        return labelPanel;
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel() {
        /* A button for adding new messages to the table. */
        JButton add = new JButton(JMeterUtils.getResString("add")); //$NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); //$NON-NLS-1$
        delete.setActionCommand(DELETE);

        checkDeleteStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(delete);
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    protected void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeLabelPanel(), BorderLayout.NORTH);
        add(makeMainPanel(), BorderLayout.CENTER);
        add(makeButtonPanel(), BorderLayout.SOUTH);

        table.revalidate();
    }

    private static class MessageTypeCelEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 1L;

        public MessageTypeCelEditor() {
            super (new JComboBox<>(new String[]{
                AzAmqpMessages.MESSAGE_TYPE_STRING,
                AzAmqpMessages.MESSAGE_TYPE_BASE64,
                AzAmqpMessages.MESSAGE_TYPE_FILE,
                AzAmqpMessages.MESSAGE_TYPE_BYTES

            }));
        }

    }

}
