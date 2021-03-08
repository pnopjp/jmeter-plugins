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
package jp.co.pnop.jmeter.protocol.azureeventhubs.control.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

import jp.co.pnop.jmeter.protocol.azureeventhubs.config.gui.AzEventHubsConfigGui;
import jp.co.pnop.jmeter.protocol.azureeventhubs.sampler.AzEventHubsSampler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzEventHubsSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AzEventHubsSamplerGui.class);

    private AzEventHubsConfigGui azEventHubsConfigGui;

    public AzEventHubsSamplerGui() {
        init();
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
        azEventHubsConfigGui.configure(element);
    }

    @Override
    public TestElement createTestElement() {
        AzEventHubsSampler sampler = new AzEventHubsSampler();
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
        sampler.addTestElement(azEventHubsConfigGui.createTestElement());
        super.configureTestElement(sampler);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        azEventHubsConfigGui.clearGui();
    }

    @Override
    public String getLabelResource() {
        return null; // $NON-NLS-1$
    }

    public String getStaticLabel() {
        return "Azure Event Hubs Sampler";
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        azEventHubsConfigGui = new AzEventHubsConfigGui(false);
        mainPanel.add(azEventHubsConfigGui);
        add(mainPanel, BorderLayout.CENTER);
    }

}
