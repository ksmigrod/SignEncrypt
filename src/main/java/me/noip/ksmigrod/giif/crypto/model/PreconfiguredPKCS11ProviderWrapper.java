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

import java.security.Provider;

public class PreconfiguredPKCS11ProviderWrapper extends PKCS11ProviderWrapper {

    public PreconfiguredPKCS11ProviderWrapper(Provider provider) {
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
        return false;
    }

    @Override
    public boolean isUnloadable() {
        return false;
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException(
                "Predefined PKCS11 Provider cannot be loaded.");
    }

    @Override
    public void unload() {
        throw new UnsupportedOperationException(
                "Predefined PKCS11 Provider cannot be unloaded");
    }

    @Override
    public boolean isBuiltIn() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PreconfiguredPKCS11ProviderWrapper) {
            PreconfiguredPKCS11ProviderWrapper p = (PreconfiguredPKCS11ProviderWrapper) obj;
            return provider.equals(p.getProvider());
        } else {
            return false;
        }
    }
}
