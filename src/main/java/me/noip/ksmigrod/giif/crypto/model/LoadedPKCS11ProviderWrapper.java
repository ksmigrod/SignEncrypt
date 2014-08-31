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

import java.io.File;
import java.security.AuthProvider;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;

import javax.security.auth.login.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadedPKCS11ProviderWrapper extends PKCS11ProviderWrapper {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String providerName;
    private final String providerParameter;
    private boolean loaded;

    public LoadedPKCS11ProviderWrapper(Provider provider, String providerParameter) {
        this.provider = provider;
        this.providerName = provider.getName();
        this.providerParameter = providerParameter;
        loaded = true;
    }

    public LoadedPKCS11ProviderWrapper(String providerName, String providerParameter) {
        this.providerName = providerName;
        this.providerParameter = providerParameter;
        provider = null;
        loaded = false;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getProviderParameter() {
        return providerParameter;
    }

    @Override
    public Boolean isLoaded() {
        return loaded;
    }

    @Override
    public boolean isLoadable() {
        return !loaded
                && new File(providerParameter).canRead()
                && Security.getProvider(providerName) == null;
    }

    @Override
    public boolean isUnloadable() {
        return loaded;
    }

    @Override
    public void load() {
        assert (provider == null && !loaded);
        log.debug("Loading PKCS#11 provider {} from config file {}", providerName, providerParameter);
        Provider loadedProvider = new sun.security.pkcs11.SunPKCS11(providerParameter);
        if (Security.getProvider(loadedProvider.getName()) != null) {
            throw new IllegalStateException("Provider " + loadedProvider.getName() + " already loaded.");
        }
        Security.addProvider(loadedProvider);
        providerName = loadedProvider.getName();
        provider = loadedProvider;
        loaded = true;
        log.debug("Provider {} loaded from config file {}", providerName, providerParameter);
    }

    @Override
    public void unload() {
        assert (provider != null && loaded);
        log.debug("Unloading provider {}.", providerName);
        Security.removeProvider(providerName);
        try {
            ((AuthProvider) provider).logout();
        } catch (LoginException e) {
            log.error("Logging out from PKCS#11 token on unload", e);
        }
        provider = null;
        loaded = false;
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = true;
        if (obj instanceof LoadedPKCS11ProviderWrapper) {
            LoadedPKCS11ProviderWrapper p = (LoadedPKCS11ProviderWrapper) obj;
            result = providerName.equals(p.getProviderName());
            result = result && providerParameter.equals(p.getProviderParameter());
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.providerName);
        hash = 89 * hash + Objects.hashCode(this.providerParameter);
        return hash;
    }
    
    
}
