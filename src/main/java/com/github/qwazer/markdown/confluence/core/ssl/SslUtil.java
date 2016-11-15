package com.github.qwazer.markdown.confluence.core.ssl;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class SslUtil {

    public static void sslTrustAll() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // Trust always
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // Trust always
                    }
                }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        // Create empty HostnameVerifier
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        };

        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
}
