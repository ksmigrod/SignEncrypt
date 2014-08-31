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
import java.awt.Dimension;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.LoggerFactory;
import me.noip.ksmigrod.giif.crypto.model.CertInfo;
import me.noip.ksmigrod.giif.crypto.model.CertificateWithKeystore;
import me.noip.ksmigrod.giif.crypto.model.CertificatesTableModel;
import me.noip.ksmigrod.giif.crypto.model.ProviderWrapper;

/**
 * Certificate choice wizzard page.
 *
 * Preconditions: "provider" : ProviderWrapper "certificate" : if set, then from
 * provider's keystore choose entry with equal certificate.
 *
 * Postconditions: "certificate" : CertificateWithKeyStore
 *
 * If ProviderWrapper requires PIN to retrieve certificates from its key store
 * then pin dialog is presented.
 *
 * @author ksm
 */
public class SignCertificateChoicePage extends WizardPage {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    private final JTable certificatesTable;
    private final CertificatesTableModel certificatesModel;
    private final JLabel subjectLabel;
    private final JTextField subjectText;
    private final JLabel notBeforeLabel;
    private final JTextField notBeforeText;
    private final JLabel notAfterLabel;
    private final JTextField notAfterText;

    public SignCertificateChoicePage() {
        super(Msg.getString("CERTIFICATE_CHOICE"), Msg.getString("CHOOSE_CERTIFICATE"));
        setLayout(new MigLayout("ins panel", "[][grow,fill][][grow,fill]", "[al top,grow][][]"));

        certificatesModel = new CertificatesTableModel();
        certificatesTable = new JTable(certificatesModel);
        certificatesTable.getColumnModel().getColumn(0).setMinWidth(60);
        certificatesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        certificatesTable.getColumnModel().getColumn(0).setMaxWidth(250);
        certificatesTable.getColumnModel().getColumn(1).setMinWidth(200);
        certificatesTable.getColumnModel().getColumn(1).setPreferredWidth(500);
        certificatesTable.getColumnModel().getColumn(1).setMaxWidth(32767);
        certificatesTable.setPreferredScrollableViewportSize(new Dimension(800, 250));
        JScrollPane jsp = new JScrollPane(certificatesTable);
        add(jsp, "pushy, spanx 4, wrap");

        subjectLabel = new JLabel(Msg.getString("CERTIFICATE_SUBJECT"));
        subjectText = new JTextField(100);
        subjectText.setEditable(false);
        subjectLabel.setLabelFor(subjectText);
        add(subjectLabel);
        add(subjectText, "gap rel,spanx 3, wrap");

        notBeforeLabel = new JLabel(Msg.getString("CERTIFICATE_NOT_BEFORE"));
        notBeforeText = new JTextField(20);
        notBeforeText.setEditable(false);
        notBeforeLabel.setLabelFor(notBeforeText);
        add(notBeforeLabel);
        add(notBeforeText, "gap rel");

        notAfterLabel = new JLabel(Msg.getString("CERTIFICATE_NOT_AFTER"));
        notAfterText = new JTextField(20);
        notAfterText.setEditable(false);
        notAfterLabel.setLabelFor(notAfterText);
        add(notAfterLabel, "gap unrel");
        add(notAfterText, "gap rel,wrap");

        certificatesTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        for (int i = e.getFirstIndex();
                        i <= e.getLastIndex(); i++) {
                            if (certificatesTable.getSelectionModel().isSelectedIndex(i)) {
                                X509Certificate cert = certificatesModel.getCertificate(i).getCertificate();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date now = new Date();
                                subjectText.setText(CertInfo.subject(cert));
                                notBeforeText.setText(sdf.format(cert.getNotBefore()));
                                notBeforeText.setForeground(now.before(cert.getNotBefore()) ? Color.RED : UIManager.getColor("TextField.foreground"));
                                notAfterText.setText(sdf.format(cert.getNotAfter()));
                                notAfterText.setForeground(now.after(cert.getNotAfter()) ? Color.RED : UIManager.getColor("TextField.foreground"));
                            }
                        }
                    }
                }
        );
    }

    private void setSelectedCertificate(CertificateWithKeystore cert) {
        int index = certificatesModel.indexOf(cert);
        if (index != -1) {
            certificatesTable.setRowSelectionInterval(index, index);
        }
    }

    private CertificateWithKeystore getSelectedCertificate() {
        int index = certificatesTable.getSelectedRow();
        if (index != -1) {
            return certificatesModel.getCertificate(index);
        } else {
            return null;
        }
    }

    @Override
    public void rendering(List<WizardPage> path, final WizardSettings settings) {
        super.rendering(path, settings); //To change body of generated methods, choose Tools | Templates.
        Object prObj = settings.get("provider");
        if (prObj instanceof ProviderWrapper) {
            final ProviderWrapper provider = (ProviderWrapper) prObj;
            (new SwingWorker<List<CertificateWithKeystore>, Void>() {

                @Override
                protected List<CertificateWithKeystore> doInBackground() throws Exception {
                    List<CertificateWithKeystore> result;
                    while (true) {
                        try {
                            result = provider.getCertificates();
                            break;
                        } catch (IllegalStateException ex) {
                            if (Msg.exceptionDialog(getTopLevelAncestor(), Msg.getString("CERTIFICATES_CANT_READ"), ex)) {
                                System.exit(-1);
                            }
                        }
                    }
                    return result;
                }

                @Override
                protected void done() {
                    try {
                        certificatesModel.setCertificates(get());
                        Object crObj = settings.get("certificate");
                        if (crObj instanceof CertificateWithKeystore) {
                            CertificateWithKeystore cert = (CertificateWithKeystore) crObj;
                            setSelectedCertificate(cert);
                        } else {
                            selectFirstValidCertificate();
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        if (Msg.exceptionDialog(getTopLevelAncestor(), Msg.getString("CERTIFICATES_CANT_READ"), ex)) {
                            System.exit(-1);
                        }
                    }
                }
            }).execute();
        }
    }

    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings); //To change body of generated methods, choose Tools | Templates.
        settings.put("certificate",getSelectedCertificate());
    }

    private void selectFirstValidCertificate() {
        int idx = certificatesModel.getValidCertificateIndex();
        if (idx != -1) {
            certificatesTable.setRowSelectionInterval(idx, idx);
        }
    }

}
