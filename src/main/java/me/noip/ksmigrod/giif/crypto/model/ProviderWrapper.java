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
package me.noip.ksmigrod.giif.crypto.model;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProviderWrapper {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    protected Provider provider;
    protected KeyStore keyStore;

    public abstract String getProviderName();

    public abstract String getProviderParameter();

    public abstract Boolean isLoaded();

    public abstract boolean isLoadable();

    public abstract boolean isUnloadable();

    public abstract void load();

    public abstract void unload();

    public abstract boolean isBuiltIn();

    public Provider getProvider() {
        return provider;
    }
    
    protected abstract KeyStore acquireKeyStore() throws KeyStoreException;
    
    public List<CertificateWithKeystore> getCertificates() {
        log.trace("ProviderWrapper.getCertificates()");
        List<CertificateWithKeystore> result = new ArrayList<CertificateWithKeystore>();
        try {
            KeyStore ks = acquireKeyStore();
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
                if (log.isDebugEnabled()) {
                    log.debug("Loaded certificate with alias {}, issued by {},"
                            + " serial {}, to {}", 
                            alias,
                            CertInfo.issuer(cert),
                            cert.getSerialNumber().toString(16),
                            CertInfo.subject(cert));
                } 
                if (!ks.isKeyEntry(alias)) {
                    log.info("No key in for {} in key store, certificate rejected.", alias);
                }
                if (!CertInfo.isNonRepudationOnly(cert)) {
                    log.info("Certificate {} is not NonRepudation ONLY", alias);
                }
                result.add(new CertificateWithKeystore(ks, alias, cert));
                log.info("Added certificate {}", alias);
            }
        } catch (KeyStoreException ex) {
            log.error("Loading certificates.", ex);
            throw new IllegalStateException(ex);
        }
        return result;
    }

}
