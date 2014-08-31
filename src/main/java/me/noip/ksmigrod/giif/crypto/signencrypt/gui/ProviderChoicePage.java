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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.LoggerFactory;
import me.noip.ksmigrod.giif.crypto.model.LoadedPKCS11ProviderWrapper;
import me.noip.ksmigrod.giif.crypto.model.ProviderWrapper;
import me.noip.ksmigrod.giif.crypto.model.ProvidersTableModel;

/**
 * Provider choice wizard page.
 *
 * Preconditions: None
 *
 * Postconditions: "provider" : ProviderWrapper with choosen certificate.
 *
 * Wizard page stores loaded providers in java.util.prefs API. 
 *
 * @author ksm
 */
public class ProviderChoicePage extends WizardPage {

    private static final long serialVersionUID = 1L;

    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass());

    private final JTable providersTable;
    private final ProvidersTableModel providersModel;
    private final JButton addButton;
    private final JButton delButton;
    private final JButton loadButton;
    private final JButton unloadButton;

    /**
     * Select configuration file and load provider it describes.
     * 
     * Shows "Open File" dialog, allows selection of configuration file.
     * Loads provider based on configuration file.
     */
    private class AddProviderAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        public AddProviderAction() {
            super(Msg.getString("PROVIDER_ADD_BTN"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(Msg.getString("PKCS11_CONFIG_FILE"), "cfg"));
            int returnVal = fc.showOpenDialog(ProviderChoicePage.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File confFile = fc.getSelectedFile();
                if (!confFile.canRead()) {
                    JOptionPane.showMessageDialog(ProviderChoicePage.this,
                            Msg.getString("PROVIDER_ADD_NOT_READABLE"),
                            Msg.getString("PROVIDER_ADD_ERROR"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try (InputStream is = new FileInputStream(confFile)) {
                    @SuppressWarnings("restriction")
                    Provider newProvider = new sun.security.pkcs11.SunPKCS11(is);
                    if (Security.getProvider(newProvider.getName()) == null) {
                        Security.addProvider(newProvider);
                        providersModel.add(new LoadedPKCS11ProviderWrapper(
                                newProvider, confFile.getCanonicalPath()));
                        providersTable.setRowSelectionInterval(
                                providersModel.getRowCount() - 1,
                                providersModel.getRowCount() - 1);
                        providersModel.saveList();
                    } else {
                        JOptionPane
                                .showMessageDialog(
                                        ProviderChoicePage.this,
                                        String.format(Msg.getString("PROVIDER_ALREADY_LOADED"),
                                                newProvider.getName()),
                                        Msg.getString("PROVIDER_ADD_ERROR"),
                                        JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    Msg.exceptionDialog(ProviderChoicePage.this, Msg.getString("PROVIDER_ADD_ERROR"), ex);
                }
            }
        }
    }

    private class DeleteProviderAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public DeleteProviderAction() {
            super(Msg.getString("PROVIDER_DEL_BTN"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = providersTable.getSelectionModel().getMinSelectionIndex();
            if (selectedIndex != -1) {
                providersModel.remove(selectedIndex);
            }
            loadButton.setEnabled(false);
            unloadButton.setEnabled(false);
            delButton.setEnabled(false);
            setNextEnabled(false);
        }

    }

    private class LoadProviderAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public LoadProviderAction() {
            super(Msg.getString("PROVIDER_LOAD_BTN"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final int selectedIndex = providersTable.getSelectionModel().getMinSelectionIndex();
            if (selectedIndex != -1) {
                (new SwingWorker<Void, Void>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        providersModel.load(selectedIndex);
                        return null;
                    }

                    @Override
                    protected void done() {
                        loadButton.setEnabled(false);
                        unloadButton.setEnabled(true);
                        setNextEnabled(true);
                    }

                }).execute();
            }
        }
    }

    private class UnloadProviderAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public UnloadProviderAction() {
            super(Msg.getString("PROVIDER_UNLOAD_BTN"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = providersTable.getSelectionModel().getMinSelectionIndex();
            if (selectedIndex != -1) {
                providersModel.unload(selectedIndex);
                loadButton.setEnabled(true);
                unloadButton.setEnabled(false);
                setNextEnabled(false);
            }
        }

    }

    public ProviderChoicePage() {
        super(Msg.getString("CRYPTO_PROVIDER"), Msg.getString("CHOOSE_CRYPTO_PROVIDER"));
        this.setLayout(new MigLayout("", "[grow,fill][fill]",
                "[][][][grow,align top]"));

        providersModel = new ProvidersTableModel();
        providersTable = new JTable(providersModel);
        JScrollPane providersScrollPane = new JScrollPane(providersTable);
        add(providersScrollPane, "spany 4");

        addButton = new JButton(new AddProviderAction());
        add(addButton, "wrap");
        delButton = new JButton(new DeleteProviderAction());
        add(delButton, "wrap");
        loadButton = new JButton(new LoadProviderAction());
        add(loadButton, "wrap");
        unloadButton = new JButton(new UnloadProviderAction());
        add(unloadButton, "wrap");
        providersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        providersTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        log.info("Selection changed");
                        for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                            if (providersTable.getSelectionModel()
                            .isSelectedIndex(i)) {
                                ProviderWrapper selected = providersModel
                                .get(i);
                                loadButton.setEnabled(selected.isLoadable());
                                unloadButton.setEnabled(selected.isUnloadable());
                                delButton.setEnabled(!selected.isBuiltIn());
                                ProviderChoicePage.this.setNextEnabled(selected.isLoaded());
                                return;
                            }
                        }
                    }
                });
        if (providersModel.getRowCount() > 0) {
            providersTable.setRowSelectionInterval(0, 0);
        }
        providersTable.getColumnModel().getColumn(0).setMinWidth(10);
        providersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        providersTable.getColumnModel().getColumn(0).setMaxWidth(80);
        providersTable.getColumnModel().getColumn(1).setMinWidth(100);
        providersTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        providersTable.getColumnModel().getColumn(1).setMaxWidth(200);
        providersTable.getColumnModel().getColumn(0).setMinWidth(200);
        providersTable.getColumnModel().getColumn(0).setPreferredWidth(400);
        providersTable.getColumnModel().getColumn(0).setMaxWidth(32767);
    }

    public ProviderWrapper getSelectedProvider() {
        int selectedIndex = providersTable.getSelectedRow();
        if (selectedIndex != -1) {
            return providersModel.get(selectedIndex);
        } else {
            return null;
        }
    }

    public void setSelectedProvider(ProviderWrapper p) {
        int idx = providersModel.indexOf(p);
        if (idx > -1) {
            providersTable.setRowSelectionInterval(idx, idx);
        }
    }

    @Override
    public void rendering(List<WizardPage> path, WizardSettings settings) {
        super.rendering(path, settings);
        log.info("rendering");
        Object p = settings.get("provider");
        if (p instanceof ProviderWrapper) {
            ProviderWrapper providerWrapper = (ProviderWrapper) p;
            setSelectedProvider(providerWrapper);
        } else {
            ProviderWrapper providerWrapper = getSelectedProvider();
            if (providerWrapper != null) {
                setNextEnabled(providerWrapper.isLoaded());
            }
        }
    }

    @Override
    public void updateSettings(WizardSettings settings) {
        super.updateSettings(settings);
        settings.put("provider", getSelectedProvider());
    }

}
