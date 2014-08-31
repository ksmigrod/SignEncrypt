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
package me.noip.ksmigrod.giif.crypto.signencrypt;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 *
 * @author ksm
 */
public class TestCallbackHandler implements CallbackHandler {

    private static final Logger logger = Logger.getLogger(TestCallbackHandler.class.getName());    
    
    private final String password;
    
    public TestCallbackHandler(String password) {
        this.password = password;
    }
    
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callback;
                pc.setPassword(password.toCharArray());
                logger.log(Level.INFO, "PasswordCallback: prompt is \"{0}\", password set to \"{1}\"", new Object[]{pc.getPrompt(), password});
            } else {
                logger.log(Level.WARNING, "Unsupported callback: {0}", callback.getClass().getName());
            }
        }
    }
    
}
