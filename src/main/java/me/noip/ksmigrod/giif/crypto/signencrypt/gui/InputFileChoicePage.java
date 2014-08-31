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
import java.util.prefs.BackingStoreException;
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
public class InputFileChoicePage extends WizardPage {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());
    
    private final JLabel inputFileLabel;
    private final JTextField inputFileText;
    private final JButton inputFileBtn;
    private File currentFolder;
    private File selectedFile;

    private class SavePreference extends SwingWorker<Void, Void> {

        private final String name;
        private final String value;

        public SavePreference(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            Preferences prefs = Preferences.userNodeForPackage(InputFileChoicePage.class);
            prefs.put(name, value);
            return null;
        }
        
    }
    
    private class FileChoiceAction extends AbstractAction {

        public FileChoiceAction() {
            super(Msg.getString("INPUT_FILE_BTN"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    Msg.getString("TEXT_AND_XML_FILES"), "xml", "txt");
            chooser.setFileFilter(filter);
            if (currentFolder != null) {
                chooser.setCurrentDirectory(currentFolder);
            }
            int returnVal = chooser.showOpenDialog(InputFileChoicePage.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                if (selectedFile.canRead()) {
                    inputFileText.setText(selectedFile.getPath());
                    currentFolder = selectedFile.getParentFile();
                }
                setNextEnabled(true);
           }
        }

    }

    public InputFileChoicePage() {
        super(Msg.getString("CHOOSE_INPUT_FILE"), Msg.getString("INPUT_FILE_CHOICE"));
        setLayout(new MigLayout());

        inputFileLabel = new JLabel(Msg.getString("INPUT_FILE_LABEL"));
        inputFileText = new JTextField(255);
        inputFileLabel.setLabelFor(inputFileText);
        inputFileBtn = new JButton(new FileChoiceAction());
        
        inputFileText.setEditable(false);
        
        add(inputFileLabel);
        add(inputFileText, "pushx");
        add(inputFileBtn, "wrap");        
    }
    
    private void reset() {
        inputFileText.setText("");
        selectedFile = null;
        currentFolder = null;
    }
    
    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        reset();
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        try {
            if (prefs.nodeExists("inputFolder")) {
                String currentFolderName = prefs.get("inputFolder", null);
                File folder = new File(currentFolderName);
                if (folder.isDirectory()) {
                    currentFolder = folder;
                }
            }
        } catch (BackingStoreException ex) {
            log.error("Reading inputFolder from preferences", ex);
        }
        boolean enableNext = false;
        if (settings.containsKey("inputFile")) {
            Object obj = settings.get("inputFile");
            if (obj instanceof File) {
                File file = (File) obj;
                if (file.canRead()) {
                    selectedFile = file;
                    inputFileText.setText(file.getPath());
                    enableNext = true;
                }
            }
        }
        setNextEnabled(enableNext);
    }

    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings); //To change body of generated methods, choose Tools | Templates.
        if (selectedFile != null) {
            settings.put("inputFile", selectedFile);
        }
        if (currentFolder != null) {
            (new SavePreference("inputFolder", currentFolder.getPath())).execute();
        }
        reset();
    }
}
