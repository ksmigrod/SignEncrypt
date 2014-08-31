/* 
 * Copyright 2014 Krzysztof Śmigrodzki.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.noip.ksmigrod.giif.crypto.signencrypt.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ksm
 */
class OutputFileChoicePage extends WizardPage {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    private final JLabel outputFileLabel;
    private final JTextField outputFileText;
    private final JButton outputFileBtn;

    private boolean fileSelected = false;

    private File outputFolder = null;
    private File outputFile = null;

    public OutputFileChoicePage() {
        super(Msg.getString("CHOOSE_OUTPUT_FILE"), Msg.getString("OUTPUT_FILE_CHOICE"));

        setLayout(new MigLayout());

        outputFileLabel = new JLabel(Msg.getString("OUTPUT_FILE_LABEL"));
        outputFileText = new JTextField(255);
        outputFileText.setEditable(false);
        outputFileLabel.setLabelFor(outputFileText);
        outputFileBtn = new JButton(new OutputFileAction());

        add(outputFileLabel);
        add(outputFileText, "pushx");
        add(outputFileBtn, "wrap");

    }

    private class SavePreference extends SwingWorker<Void, Void> {

        private final String name;
        private final String value;

        public SavePreference(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        protected Void doInBackground() throws Exception {
            Preferences prefs = Preferences.userNodeForPackage(EncryptionCertificateChoicePage.class);
            prefs.put(name, value);
            return null;
        }

    }

    private class OutputFileAction extends AbstractAction {

        public OutputFileAction() {
            super(Msg.getString("OUTPUT_FILE_BTN"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    Msg.getString("ENCRYPTED_FILES"), "enc");
            chooser.setFileFilter(filter);
            if (outputFolder != null) {
                chooser.setCurrentDirectory(outputFolder);
            }
            int returnVal = chooser.showSaveDialog(OutputFileChoicePage.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                outputFile = chooser.getSelectedFile();
                outputFileText.setText(outputFile.getPath());
                outputFolder = outputFile.getParentFile();
                setNextEnabled(true);
            }
        }
    }

    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings); //To change body of generated methods, choose Tools | Templates.
        if (outputFolder != null) {
            (new SavePreference("outputFolder", outputFolder.getPath())).execute();
        }
        settings.put("outputFile", outputFile);
        reset();
    }

    private void reset() {
        outputFile = null;
        outputFolder = null;
        outputFileText.setText("");
        setNextEnabled(false);
        setFinishEnabled(false);
    }

    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        reset();
        File inFile = (File) settings.get("inputFile");
        outputFolder = inFile.getParentFile();
        Preferences prefs = Preferences.userNodeForPackage(OutputFileChoicePage.class);
        String outputFolderName = prefs.get("outputFolder", null);
        if (outputFolderName != null) {
            File tmp = new File(outputFolderName);
            if (tmp.isDirectory()) {
                outputFolder = tmp;
            }
        }
   }

}
