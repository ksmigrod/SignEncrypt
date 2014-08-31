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

import java.io.Console;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 *
 * @author ksm
 */
public class ConsoleCallbackHandler implements CallbackHandler {

    private static final Logger logger = Logger.getLogger(ConsoleCallbackHandler.class.getName());
    
    public ConsoleCallbackHandler() {
        if (System.console() == null) {
            throw new UnsupportedOperationException("Console not available.");
        }
    }
    
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        Console con = System.console();
        for (Callback callback : callbacks) {
            if (callback instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callback;
                String prompt = pc.getPrompt();
                prompt = prompt != null && ! prompt.isEmpty() ? prompt : "PIN";
                con.format("%s: ", prompt);
                logger.log(Level.INFO, 
                        "Password callback with prompt: {0}", 
                        prompt);
                String password = con.readLine();
                pc.setPassword(password.toCharArray());
            } else if (callback instanceof TextOutputCallback) {
                TextOutputCallback tc = (TextOutputCallback) callback;
                con.format("%s", tc.getMessage());
                logger.log(Level.INFO, 
                        "TextOutputCallback type {0} message: {1}", 
                        new Object[]{tc.getMessageType(), tc.getMessage()});
            } else if (callback instanceof NameCallback) {
                NameCallback nc = (NameCallback) callback;
                String prompt = nc.getPrompt();
                prompt = prompt != null && ! prompt.isEmpty() ? prompt : "Name";
                String defaultName = nc.getDefaultName();
                if (defaultName != null && ! defaultName.isEmpty()) {
                    
                }
            } else {
                logger.log(Level.WARNING,
                        "Unknown callback type {0}",
                        callback.getClass().getName());;
            }
        }
    }

}
