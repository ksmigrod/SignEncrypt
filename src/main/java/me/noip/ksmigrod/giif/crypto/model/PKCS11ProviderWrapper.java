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
import me.noip.ksmigrod.giif.crypto.signencrypt.SwingCallbackHandler;

public abstract class PKCS11ProviderWrapper extends ProviderWrapper {
	    
    @Override
    protected KeyStore acquireKeyStore() throws KeyStoreException {
        if (keyStore == null) {
            KeyStore.CallbackHandlerProtection chp
                    = new KeyStore.CallbackHandlerProtection(new SwingCallbackHandler());
            KeyStore.Builder builder
                    = KeyStore.Builder.newInstance("PKCS11", provider, chp);
            keyStore = builder.getKeyStore();
        }
        return keyStore;
    }

}
