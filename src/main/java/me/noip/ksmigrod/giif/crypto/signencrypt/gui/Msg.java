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

import java.awt.Container;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ksm
 */
public class Msg {

    private static final ResourceBundle messages;
    private static final Logger log = LoggerFactory.getLogger(Msg.class);
    
    static {
        messages = java.util.ResourceBundle.getBundle("pl.gov.mofnet.giif.crypto.signeencrypt.gui.messages");
    }

    public static final String getString(String key) {
        if (messages.containsKey(key)) {
            return messages.getString(key);
        } else {
            return '#'+key+'#';
        }
    }
    
    /**
     * Show stack trace dialog with option to exit application.
     * 
     * @param title Dialog title.
     * @param ex Excepton to show stack trace of
     * @return true if user requested to terminate application
     */
    public static boolean exceptionDialog(String title, Throwable ex) {
        return exceptionDialog(null, title, ex);
    }

    /**
     * Show stack trace dialog with option to exit application.
     * 
     * @param cnt Parent conainer
     * @param title Dialog title.
     * @param ex Excepton to show stack trace of
     * @return true if user requested to terminate application
     */    
    public static boolean exceptionDialog(Container cnt, String title, Throwable ex) {
        boolean result = false;
        PrintStream ps = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(10240);
            ps = new PrintStream(bos, false, "UTF-8");
            ex.printStackTrace(ps);
            JTextArea jta = new JTextArea(new String(bos.toByteArray(), StandardCharsets.UTF_8));
            JScrollPane jsp = new JScrollPane(jta){
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(480, 320);
                }
            };
            Object[] options = new Object[]{Msg.getString("EXIT_APPLICATION"), Msg.getString("TRY_AGAIN")};
            int dlgResult = JOptionPane.showOptionDialog(
                    cnt, 
                    jsp, 
                    title,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[1]);
            if (dlgResult == JOptionPane.OK_OPTION) {
                result = true;
            }
        } catch (UnsupportedEncodingException ex1) {
            log.error("", ex);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
        return result;
    }
}
