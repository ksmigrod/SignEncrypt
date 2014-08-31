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

import java.util.List;
import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 *
 * @author ksm
 */
class SignEncryptPageFactory implements PageFactory {

    public SignEncryptPageFactory() {

    }

    /**
     * Gets last page in path.
     * 
     * @param path Path of wisited wizard pages.
     * @return previously visited page.
     */
    private static WizardPage lastPage(List<WizardPage> path) {
        return path.get(path.size() - 1);
    }
    
    /**
     * Creates new page based on lastPage and settings.
     * 
     * @param path visited pages
     * @param settings settings acquired from previous pages
     * @return new page do display
     */
    @Override
    public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
        if (path == null || path.isEmpty()) {
            // Start with provider choice.
            return new ProviderChoicePage();
        } else if (lastPage(path) instanceof ProviderChoicePage) {
            // Choose certificate stored in provider's keystore.
            return new SignCertificateChoicePage();
        } else if (lastPage(path) instanceof SignCertificateChoicePage) {
            // Choose DER or PEM certificate for encryption.
            return new EncryptionCertificateChoicePage();
        } else if (lastPage(path) instanceof EncryptionCertificateChoicePage) {
            // Choose file to be signed and encrypted.
            return new InputFileChoicePage();
        } else if (lastPage(path) instanceof InputFileChoicePage) {
            // Choose result file.
            return new OutputFileChoicePage();
        } else if (lastPage(path) instanceof OutputFileChoicePage) {
            // Sign, encrypt and write to file.
            return new FinishedPage();
        } else if (lastPage(path) instanceof FinishedPage) {
            // if Next was choosen, then choose next file.
            return new InputFileChoicePage();
        } else {
            return null;
        }
    }
    
}
