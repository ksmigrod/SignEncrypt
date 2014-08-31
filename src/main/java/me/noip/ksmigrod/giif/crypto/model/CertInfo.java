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

import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import javax.security.auth.x500.X500Principal;

/**
 * @author ksm
 */
public class CertInfo {

    private static String remapOIDS(String paramString) {
        return paramString.replaceFirst("OID.2.5.4.5=", "SN=").replaceAll("OID.2.5.4.42=", "G=").replaceAll("OID.2.5.4.4=", "S=");
    }

    private static String reverseDN(String paramString) {
        StringBuilder localStringBuffer = new StringBuilder(paramString.length());
        for (StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ","); localStringTokenizer.hasMoreTokens();) {
            String str = localStringTokenizer.nextToken().trim();
            if (!str.startsWith("OID.2.5.4.16")) {
                localStringBuffer.insert(0, str);
                localStringBuffer.insert(0, '/');
            }
        }
        return localStringBuffer.toString();
    }

    public static String issuer(X509Certificate cert) {
        String str = cert.getIssuerX500Principal().getName(X500Principal.RFC1779);
        str = remapOIDS(str);
        return reverseDN(str);
    }

    public static String subject(X509Certificate cert) {
        String str = cert.getSubjectX500Principal().getName(X500Principal.RFC1779);
        str = remapOIDS(str);
        return reverseDN(str);
    }

    public static boolean isNonRepudationOnly(X509Certificate cert) {
        boolean[] keyUsage = cert.getKeyUsage();
        if (keyUsage == null) {
            return false;
        }
        int sum = 0;
        for (boolean b : keyUsage) {
            sum += b ? 1 : 0;
        }
        return keyUsage[1] && sum == 1;
    }

}
