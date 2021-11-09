package jp.co.pnop.jmeter.protocol.azureservicebus.config.gui;

import java.awt.BorderLayout;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.gui.JLabeledTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.pnop.jmeter.protocol.azureservicebus.common.AzServiceBusClientParams;
import jp.co.pnop.jmeter.protocol.azureservicebus.common.gui.AzServiceBusClientParamsPanel;
import jp.co.pnop.jmeter.protocol.azureservicebus.config.AzServiceBusClient;

public class AzServiceBusClientGui extends AbstractConfigGui {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzServiceBusClientGui.class);

    private AzServiceBusClientParamsPanel sbclientPanel = new AzServiceBusClientParamsPanel(false);
    private JLabeledTextField connectionName;

    private static AtomicInteger classCount = new AtomicInteger(0); // keep track of classes created

    public AzServiceBusClientGui() {
        init();
        classCount.incrementAndGet();
        trace("AzServiceBusTransactionGui()");
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

        sbclientPanel.configure((TestElement)element.getProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS).getObjectValue());
        connectionName.setText(element.getPropertyAsString(AzServiceBusClientParams.CONNECTION_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return null;
    }

    public String getStaticLabel() {
        return "Azure Service Bus Connection";
    }

    @Override
    public TestElement createTestElement() {
        AzServiceBusClient transaction = new AzServiceBusClient();
        modifyTestElement(transaction);
        return transaction;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        element.clear();
        super.configureTestElement(element);

        element.setProperty(new TestElementProperty(AzServiceBusClientParams.SERVICEBUS_CLIENT_PARAMS, sbclientPanel.createTestElement()));
        element.setProperty(AzServiceBusClientParams.CONNECTION_NAME, connectionName.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        connectionName.setText("");
        sbclientPanel.clearGui();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        
        mainPanel.add(sbclientPanel);
        mainPanel.add(createConnectionNamePanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createConnectionNamePanel() {
        connectionName = new JLabeledTextField("Variable Name for created connection");
        connectionName.setName(AzServiceBusClientParams.CONNECTION_NAME);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(connectionName);

        return panel;
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
