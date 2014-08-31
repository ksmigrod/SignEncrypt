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
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.noip.ksmigrod.giif.crypto.signencrypt.gui.Msg;

public class ProvidersTableModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final transient Logger LOGGER;
    private final List<ProviderWrapper> providers;

    public ProvidersTableModel() {
        LOGGER = LoggerFactory
                .getLogger(ProvidersTableModel.class);
        providers = new ArrayList<ProviderWrapper>();

        Provider[] mscapiProvider = Security.getProviders("KeyStore.Windows-MY");
        if (mscapiProvider != null) {
            for (Provider provider : mscapiProvider) {
                LOGGER.info("MSCAPI provider added {}", provider.getName());

                providers.add(new MSCAPIProviderWrapper(provider));
            }
        }
        mscapiProvider = null;

        Provider[] pkcs11Provider = Security.getProviders("KeyStore.PKCS11");
        if (pkcs11Provider != null) {
            for (Provider provider : pkcs11Provider) {
                LOGGER.info("PKCS11 provider added {}", provider.getName());

                providers.add(new PreconfiguredPKCS11ProviderWrapper(provider));
            }
        }
        pkcs11Provider = null;

        String providersDefinition = java.util.prefs.Preferences.userNodeForPackage(getClass()).get("providers", "");
        for (StringTokenizer st = new StringTokenizer(providersDefinition, "|");
                st.hasMoreTokens();) {
            String providerDefinition = st.nextToken();
            int idx = providerDefinition.indexOf('/');
            if (idx > 0) {
                String providerName = providerDefinition.substring(0, idx);
                String providerParameter = providerDefinition.substring(idx + 1);
                providers.add(new LoadedPKCS11ProviderWrapper(providerName, providerParameter));
            }
        }
        providersDefinition = null;
    }

    @Override
    public int getRowCount() {
        return providers.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProviderWrapper provider = providers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return provider.isLoaded();
            case 1:
                return provider.getProviderName();
            case 2:
                return provider.getProviderParameter();
            default:
                return "Column " + columnIndex;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Msg.getString("PROVIDER_LOADED_COLUMN");
            case 1:
                return Msg.getString("PROVIDER_NAME_COLUMN");
            case 2:
                return Msg.getString("PROVIDER_PARAMETER_COLUMN");
            default:
                return super.getColumnName(column);
        }
    }

    public ProviderWrapper get(int index) {
        return providers.get(index);
    }

    public int add(ProviderWrapper pw) {
        providers.add(pw);
        int result = providers.size() - 1;
        fireTableRowsInserted(result, result);
        return result;
    }

    public void remove(int idx) {
        providers.get(idx).unload();
        providers.remove(idx);
        fireTableRowsDeleted(idx, idx);
        saveList();
    }

    public void unload(int idx) {
        if (providers.get(idx).isUnloadable()) {
            providers.get(idx).unload();
        }
        fireTableRowsUpdated(idx, idx);
    }

    public void load(int idx) {
        if (providers.get(idx).isLoadable()) {
            providers.get(idx).load();
        }
        fireTableRowsUpdated(idx, idx);
    }

    public void saveList() {
        StringBuilder sb = new StringBuilder();
        for (ProviderWrapper provider : providers) {
            if (!provider.isBuiltIn()) {
                sb.append(provider.getProviderName());
                sb.append('/');
                sb.append(provider.getProviderParameter());
                sb.append('|');
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        java.util.prefs.Preferences.userNodeForPackage(getClass()).put("providers", sb.toString());
    }

    public int indexOf(ProviderWrapper p) {
        int result = -1;
        for (int i = 0; i < providers.size(); i++) {
            ProviderWrapper providerWrapper = providers.get(i);
            if (providerWrapper == p || providerWrapper.equals(p)) {
                result = i;
                break;
            }
        }
        return result;
    }
}
