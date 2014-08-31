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
import java.util.List;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.ciscavate.cjwizard.FlatWizardSettings;
import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.ciscavate.cjwizard.pagetemplates.TitledPageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard based data sign and encrypt application.
 * 
 * This application is based on cjwizard wizard framework.
 * Look into SignEncryptPageFactory for description of control flow.
 * @author ksm
 */
public class SignEncrypt extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(SignEncrypt.class);
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set System L&F
                    UIManager.setLookAndFeel(
                            UIManager.getSystemLookAndFeelClassName());
                } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                   log.warn("Cannot instatiate native Look and Feel. Defaulting to Metal.");
                }
                SignEncrypt signEncrypt = new SignEncrypt();
                signEncrypt.setVisible(true);
            }
        });
    }

    public SignEncrypt() {
        final WizardContainer wc
                = new WizardContainer(new SignEncryptPageFactory(),
                        new TitledPageTemplate(),
                        new FlatWizardSettings());

        wc.addWizardListener(new WizardListener() {

            @Override
            public void onPageChanged(WizardPage newPage, List<WizardPage> path) {
                SignEncrypt.this.setTitle(newPage.getDescription());
            }

            @Override
            public void onFinished(List<WizardPage> path, WizardSettings settings) {
                SignEncrypt.this.dispose();
            }

            @Override
            public void onCanceled(List<WizardPage> path, WizardSettings settings) {
                SignEncrypt.this.dispose();
            }
        });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 400));
        getContentPane().add(wc);
        pack();
    }

}
