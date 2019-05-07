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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.ZlibCompressor;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.noip.ksmigrod.giif.crypto.model.CertificateWithKeystore;

/**
 * Sign and encrypt file in background, show encryption progress.
 *
 * @author ksm
 */
public class FinishedPage extends WizardPage {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JLabel saveProgressLabel;
    private final JProgressBar saveProgress;
    private SaveFileWorker saveFileWorker;

    public FinishedPage() {
        super(Msg.getString("SAVE_ENCRYPTED_FILE"), Msg.getString("SAVE_ENCRYPTED_FILE"));
        setLayout(new MigLayout("ins panel"));

        saveProgressLabel = new JLabel(Msg.getString("SAVE_PROGRESS_LABEL"));
        saveProgress = new JProgressBar();
        saveProgressLabel.setLabelFor(saveProgress);
        saveProgress.setMinimum(0);
        saveProgress.setMaximum(100);
        saveProgress.setPreferredSize(new Dimension(640, (int) saveProgress.getPreferredSize().getHeight()));

        add(saveProgressLabel);
        add(saveProgress, "pushx, wrap");
    }

    /**
     * Sign and encrypt file in background.
     */
    private class SaveFileWorker extends SwingWorker<Void, Void> {

        private final File outputFile;
        private final File inputFile;
        private final X509Certificate encCert;
        private final CertificateWithKeystore signCert;

        public SaveFileWorker(File outputFile, File inputFile, X509Certificate encCert, CertificateWithKeystore signCert) {
            this.outputFile = outputFile;
            this.inputFile = inputFile;
            this.encCert = encCert;
            this.signCert = signCert;
        }

        private byte[] digestSha256(byte []data) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                return md.digest(data);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Cannot instantiate SHA-256.", e);
            }
        }

        @Override
        protected Void doInBackground() throws Exception {
            int pos = 0;
            int fsize = (int) inputFile.length();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                CMSEnvelopedDataStreamGenerator egen = new CMSEnvelopedDataStreamGenerator();
                egen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(encCert));
                egen.setBufferSize(128 * 1024);
                try (OutputStream eos = egen.open(CMSObjectIdentifiers.compressedData, fos, new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).build())) {
                    CMSCompressedDataStreamGenerator cgen = new CMSCompressedDataStreamGenerator();
                    cgen.setBufferSize(128 * 1024);
                    try (OutputStream cos = cgen.open(CMSObjectIdentifiers.signedData, eos, new ZlibCompressor())) {
                        Store certs = new JcaCertStore(Collections.singletonList(signCert.getCertificate()));
                        ContentSigner contentSigner = new JcaContentSignerBuilder(signCert.getCertificate().getSigAlgName())
                                .setProvider(signCert.getProvider())
                                .build(signCert.getPrivateKey());

                        DefaultSignedAttributeTableGenerator signedAttributeGenerator = getESSCertIDv2Attribute();

                        CMSSignedDataStreamGenerator sgen = new CMSSignedDataStreamGenerator();

                        sgen.addSignerInfoGenerator(
                                new JcaSignerInfoGeneratorBuilder(
                                        new JcaDigestCalculatorProviderBuilder().setProvider("SUN").build())
                                        .setSignedAttributeGenerator(
                                                signedAttributeGenerator
                                        )
                                        .build(contentSigner, signCert.getCertificate()));

                        sgen.addCertificates(certs);
                        sgen.setBufferSize(128 * 1024);
                        try (OutputStream sos = sgen.open(cos, true);
                             InputStream is = new FileInputStream(inputFile)) {
                            byte[] buf = new byte[8192];
                            int bytesRead = 0;
                            while ((bytesRead = is.read(buf)) != -1) {
                                sos.write(buf, 0, bytesRead);
                                pos += bytesRead;
                                setProgress((int) ((long) pos * 100 / fsize));
                            }
                        }

                    }
                }
            } catch (Exception ex) {
                log.debug("Encryption error.", ex);
                throw ex;
            }
            return null;
        }

        private DefaultSignedAttributeTableGenerator getESSCertIDv2Attribute() throws IOException, CertificateEncodingException {
            final X500Name issuerX500Name = new X509CertificateHolder(signCert.getCertificate().getEncoded()).getIssuer();
            final GeneralName generalName = new GeneralName(issuerX500Name);
            final GeneralNames generalNames = new GeneralNames(generalName);
            final BigInteger serialNumber = signCert.getCertificate().getSerialNumber();
            final IssuerSerial issuerSerial = new IssuerSerial(generalNames, serialNumber);

            ESSCertIDv2 certid = new ESSCertIDv2(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), digestSha256(signCert.getCertificate().getEncoded()), issuerSerial);
            SigningCertificateV2 sigcert = new SigningCertificateV2(certid);
            final DERSet attrValues = new DERSet(sigcert);
            Attribute signingCertAttribute = new Attribute(PKCSObjectIdentifiers.id_aa_signingCertificateV2, attrValues);


            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(signingCertAttribute);

            return new DefaultSignedAttributeTableGenerator(new AttributeTable(v));
        }

        @Override
        protected void done() {
            super.done();
            setProgress(100);
            setNextEnabled(true);
            setFinishEnabled(true);
        }

    }

    private void reset() {
        setNextEnabled(false);
        setFinishEnabled(false);
        saveFileWorker = null;

        saveProgress.setValue(0);
    }

    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        reset();
        File outputFile = (File) settings.get("outputFile");
        File inputFile = (File) settings.get("inputFile");
        X509Certificate encCert = (X509Certificate) settings.get("encryptionCertificate");
        CertificateWithKeystore signCert = (CertificateWithKeystore) settings.get("certificate");
        saveFileWorker = new SaveFileWorker(outputFile, inputFile, encCert, signCert);
        saveFileWorker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    saveProgress.setValue((Integer) evt.getNewValue());
                }
            }
        });
        saveFileWorker.execute();
    }

    @Override
    public void updateSettings(WizardSettings settings) {
        settings.remove("inputFile");
        settings.remove("outputFile");
        reset();
    }

}
