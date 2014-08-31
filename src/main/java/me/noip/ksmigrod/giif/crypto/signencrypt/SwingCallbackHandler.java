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

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import org.bouncycastle.util.Arrays;
import org.slf4j.LoggerFactory;
import me.noip.ksmigrod.giif.crypto.signencrypt.gui.Msg;

/**
 *
 * @author ksm
 */
public class SwingCallbackHandler implements CallbackHandler {

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    public SwingCallbackHandler() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new UnsupportedOperationException("GUI not available");
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof TextOutputCallback) {
                TextOutputCallback cb = (TextOutputCallback) callback;
                log.info("TextOutputCallback");
                System.out.println("" + cb.getMessageType() + " " + cb.getMessage());
            } else if (callback instanceof NameCallback) {
                NameCallback cb = (NameCallback) callback;
                log.info("NameCallback");
                System.out.println("" + cb.getPrompt());
            } else if (callback instanceof PasswordCallback) {
                log.info("PasswordCallback");
                // prompt the user for sensitive information
                PasswordCallback pc = (PasswordCallback) callback;
                // I don't want to call password callback methods through thread
                // boundary, therefore StringBuilder use.
                final StringBuilder storedPassword = new StringBuilder();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            JPasswordField jpf = new JPasswordField();
                            JLabel label = new JLabel("Podaj pin karty");
                            JOptionPane.showConfirmDialog(null, new Object[]{label, jpf}, "PIN Password",
                                    JOptionPane.OK_CANCEL_OPTION);
                            char[] password = jpf.getPassword();
                            storedPassword.append(password);
                            // Overwrite password in GUI element.
                            Arrays.fill(password, '\u0000');
                        }
                    });
                } catch (InterruptedException ex) {
                    log.error("PIN dialog was interrupted.", ex);
                    if (Msg.exceptionDialog("PIN dialog fas interrupted.", ex)) {
                        System.exit(-1);
                    }
                } catch (InvocationTargetException ex) {
                    log.error("Error in PIN dialog.", ex);
                    if (Msg.exceptionDialog("PIN dialog problem", ex)) {
                        System.exit(-1);
                    }
                }
                pc.setPassword(storedPassword.toString().toCharArray());
                for (int i = 0; i < storedPassword.length(); i++) {
                    storedPassword.setCharAt(i, '\u0000');
                }
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }
        }
    }
}
