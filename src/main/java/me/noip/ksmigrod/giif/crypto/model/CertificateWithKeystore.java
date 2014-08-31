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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

/**
 * Stores X509Certificate, with KeyStore and alias it came from.
 * 
 * @author ksm
 */
public class CertificateWithKeystore {

    private KeyStore ks;
    private String alias;
    private X509Certificate certificate;

    /**
     * Create entry with KeyStore, alias and associated X509Certificate.
     * 
     * @param ks
     * @param alias
     * @param certificate 
     */
    public CertificateWithKeystore(KeyStore ks, 
                                   String alias, 
                                   X509Certificate certificate) 
    {
        this.ks = ks;
        this.alias = alias;
        this.certificate = certificate;
    }

    /**
     * Get KeyStore.
     * 
     * @return the ks
     */
    public KeyStore getKs() {
        return ks;
    }

    /**
     * Return alias of this certificate in KeyStore.
     * 
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Return certificate.
     * 
     * @return the certificate
     */
    public X509Certificate getCertificate() {
        return certificate;
    }
    
    /**
     * Gets provider of this keystore.
     * 
     * @return the provider
     */
    public Provider getProvider() {
        return ks.getProvider();
    }
    
    /**
     * Gets certificate issuer, in old OpenSSL format.
     * 
     * @return the certificate issuer
     */
    public String getIssuer() {
        String result = CertInfo.issuer(certificate);
        return result;
    }
    
    /**
     * Gets certificate subject, in old OpenSSL format.
     * 
     * @return
     */
    public String getSubject() {
    	String result = CertInfo.subject(certificate);
    	return result;
    }
    
    public String getSerialNumber() {
    	String result = certificate.getSerialNumber()
    							   .toString(16).toUpperCase();
    	if (result.length() % 2 == 1) {
    		return "0"+result;
    	} else {
    		return result;
    	}
    }
    
    public PrivateKey getPrivateKey() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        return (PrivateKey) ks.getKey(alias, null);
    }
}
