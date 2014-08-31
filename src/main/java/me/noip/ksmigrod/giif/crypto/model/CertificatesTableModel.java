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

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import me.noip.ksmigrod.giif.crypto.signencrypt.gui.Msg;

/**
 *
 * @author ksm
 */
public class CertificatesTableModel extends AbstractTableModel {

    private List<CertificateWithKeystore> certificates;

    @Override
    public int getRowCount() {
        return certificates != null ? certificates.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (certificates != null) {
            CertificateWithKeystore certificate = certificates.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return certificate.getSerialNumber();
                case 1:
                    return certificate.getIssuer();
                default:
                    return certificate.getCertificate();
            }
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return Msg.getString("CERTIFICATE_SERIAL");
            case 1:
                return Msg.getString("CERTIFICATE_ISSUER");
            default:
                return super.getColumnName(column);
        }
    }

    /**
     * @param certificates the certificates to set
     */
    public void setCertificates(List<CertificateWithKeystore> certificates) {
        this.certificates = certificates;
        fireTableDataChanged();
    }

    public CertificateWithKeystore getCertificate(int index) {
        return certificates.get(index);
    }

    public int indexOf(CertificateWithKeystore cert) {
        int result = -1;
        if (null != certificates && null != cert) {
            for (int i = 0; i < certificates.size(); i++) {
                CertificateWithKeystore c = certificates.get(i);
                if (c.getCertificate().equals(cert.getCertificate())) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    public int getValidCertificateIndex() {
        int result = -1;
        if (null != certificates) {
            Date now = new Date();
            for (int i = 0; i < certificates.size(); i++) {
                X509Certificate cert = certificates.get(i).getCertificate();
                if (now.after(cert.getNotAfter())) {
                    continue;
                }
                if (now.before(cert.getNotBefore())) {
                    continue;
                }
                if (CertInfo.isNonRepudationOnly(cert)) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }
}
