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

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MSCAPIProviderWrapper extends ProviderWrapper {

    public MSCAPIProviderWrapper(Provider provider) {
        this.provider = provider;
    }

    @Override
    public String getProviderName() {
        return provider.getName();
    }

    @Override
    public String getProviderParameter() {
        return "<<built in>>";
    }

    @Override
    public Boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isLoadable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUnloadable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException("MSCAPI Provider cannot be loaded");
    }

    @Override
    public void unload() {
        throw new UnsupportedOperationException("MSCAPI Provider cannot be unloaded");
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    protected KeyStore acquireKeyStore() throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("Windows-MY", provider);
        try {
            ks.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new IllegalStateException(ex);
        }
        return ks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MSCAPIProviderWrapper) {
            MSCAPIProviderWrapper mSCAPIProviderWrapper = (MSCAPIProviderWrapper) obj;
            return provider.equals(mSCAPIProviderWrapper.provider); //To change body of generated methods, choose Tools | Templates.
        } else {
            return false;
        }
    }

}
