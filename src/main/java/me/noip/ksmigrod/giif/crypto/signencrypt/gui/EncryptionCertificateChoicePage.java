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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.miginfocom.swing.MigLayout;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.LoggerFactory;
import me.noip.ksmigrod.giif.crypto.model.CertInfo;

/**
 *
 * @author ksm
 */
public class EncryptionCertificateChoicePage extends WizardPage {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    private final JLabel encryptionCertificateLabel;
    private final JTextField encryptionCertificateText;
    private final JButton encryptionCertificateBtn;

    private final JLabel issuerLabel;
    private final JTextField issuerText;
    private final JLabel serialLabel;
    private final JTextField serialText;
    private final JLabel subjectLabel;
    private final JTextField subjectText;
    private final JLabel notBeforeLabel;
    private final JTextField notBeforeText;
    private final JLabel notAfterLabel;
    private final JTextField notAfterText;

    private File encryptionCertificateFile;
    private X509Certificate encryptionCertificate;
    private File encryptionCertificateFolder;
    
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
    
    private class FileChoiceAction extends AbstractAction {

        public FileChoiceAction() {
            super(Msg.getString("INPUT_FILE_BTN"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    Msg.getString("CERTIFICATE_FILES"), "crt", "cer", "pem");
            chooser.setFileFilter(filter);
            if (encryptionCertificateFolder != null) {
                chooser.setCurrentDirectory(encryptionCertificateFolder);
            }
            int returnVal = chooser.showOpenDialog(EncryptionCertificateChoicePage.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                encryptionCertificateFile = chooser.getSelectedFile();
                encryptionCertificateText.setText(encryptionCertificateFile.getPath());
                readCertificateFromFile(encryptionCertificateFile);
           }
        }

    }    

    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings); //To change body of generated methods, choose Tools | Templates.
        Object obj = settings.get("encryptionCertificateFile");
        if (obj instanceof File) {
            encryptionCertificateFile = (File) obj;
            obj = settings.get("encryptionCertificate");
            if (obj instanceof X509Certificate) {
                setFromCertificate((X509Certificate) obj);
                encryptionCertificateText.setText(encryptionCertificateFile.getPath());
           } else {
                readCertificateFromFile(encryptionCertificateFile);
            }
        } else {
            Preferences prefs = Preferences.userNodeForPackage(EncryptionCertificateChoicePage.class);
            String fileName = prefs.get("encryptionCertificate", null);
            if (fileName != null) {
                File file = new File(fileName);
                readCertificateFromFile(file);
                
            }
        }
    }

    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        if (encryptionCertificate != null) {
            settings.put("encryptionCertificate", encryptionCertificate);
        }
        if (encryptionCertificateFile != null) {
            settings.put("encryptionCertificateFile", encryptionCertificateFile);
            new SavePreference("encryptionCertificate", encryptionCertificateFile.getPath()).execute();
        }
    }

    /**
     *
     */
    public EncryptionCertificateChoicePage() {
        super(Msg.getString("CHOOSE_ENCRYPTION_CERTIFICATE"), Msg.getString("ENCRYPTION_CERTIFICATE_CHOICE"));

        setLayout(new MigLayout("ins panel"));

        encryptionCertificateLabel = new JLabel(Msg.getString("ENCRYPTION_CERTIFICATE_LABEL"));
        encryptionCertificateText = new JTextField(255);
        encryptionCertificateText.setEditable(false);
        encryptionCertificateLabel.setLabelFor(encryptionCertificateText);
        encryptionCertificateBtn = new JButton(new FileChoiceAction());

        issuerLabel = new JLabel(Msg.getString("CERTIFICATE_ISSUER"));
        issuerText = new JTextField(255);
        issuerLabel.setLabelFor(issuerText);
        serialLabel = new JLabel(Msg.getString("CERTIFICATE_SERIAL"));
        serialText = new JTextField(80);
        serialLabel.setLabelFor(serialText);
        subjectLabel = new JLabel(Msg.getString("CERTIFICATE_SUBJECT"));
        subjectText = new JTextField(255);
        subjectLabel.setLabelFor(subjectText);
        notBeforeLabel = new JLabel(Msg.getString("CERTIFICATE_NOT_BEFORE"));
        notBeforeText = new JTextField(20);
        notBeforeLabel.setLabelFor(notBeforeText);
        notAfterLabel = new JLabel(Msg.getString("CERTIFICATE_NOT_AFTER"));
        notAfterText = new JTextField(20);
        notAfterLabel.setLabelFor(notAfterText);

        JPanel topRow = new JPanel(new MigLayout("ins panel", "[][grow][]"));
        topRow.add(encryptionCertificateLabel);
        topRow.add(encryptionCertificateText);
        topRow.add(encryptionCertificateBtn);
        add(topRow, "wrap");

        JPanel bottomRow = new JPanel(new MigLayout("wrap 4, ins panel", "[sg lbl][grow,sg txt][sg lbl][grow,sg txt]"));

        bottomRow.add(issuerLabel);
        bottomRow.add(issuerText, "spanx 3");
        bottomRow.add(serialLabel);
        bottomRow.add(serialText, "spanx 3");
        bottomRow.add(subjectLabel);
        bottomRow.add(subjectText, "spanx 3");
        bottomRow.add(notBeforeLabel);
        bottomRow.add(notBeforeText);
        bottomRow.add(notAfterLabel);
        bottomRow.add(notAfterText);
        add(bottomRow);
    }

    private void setFromCertificate(X509Certificate cert) {
        encryptionCertificate = cert;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        issuerText.setText(CertInfo.issuer(cert));
        serialText.setText(cert.getSerialNumber().toString(16));
        subjectText.setText(CertInfo.subject(cert));
        Date now = new Date();
        notBeforeText.setText(sdf.format(cert.getNotBefore()));
        notBeforeText.setForeground(now.before(cert.getNotBefore()) ? Color.RED : UIManager.getColor("TextField.foreground"));
        notAfterText.setText(sdf.format(cert.getNotAfter()));
        notAfterText.setForeground(now.after(cert.getNotAfter()) ? Color.RED : UIManager.getColor("TextField.foreground"));
    }

    private void readCertificateFromFile(File file) {
        if (!file.canRead()) {
            return;
        }
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                setFromCertificate((X509Certificate) cf.generateCertificate(is));
                encryptionCertificateFile = file;
                encryptionCertificateText.setText(file.getPath());
                setNextEnabled(true);
            } catch (IOException ex) {
                log.error("Reading certificate from file", ex);
            }
        } catch (CertificateException ex) {
            log.error("Reading certificate from file", ex);
        }
    }

}
