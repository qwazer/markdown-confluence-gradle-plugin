package com.github.qwazer.markdown.confluence.core.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class SslUtil {

    // Not intended to be instantiated
    private SslUtil() {}

    public static final X509TrustManager INSECURE_TRUST_MANAGER = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    };

    public static final HostnameVerifier INSECURE_HOSTNAME_VERIFIER = (hostname, session) -> true;

    public static final SSLContext INSECURE_SSL_CONTEXT;
    static {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { INSECURE_TRUST_MANAGER }, new java.security.SecureRandom());
            INSECURE_SSL_CONTEXT = sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void sslTrustAll() {
        HttpsURLConnection.setDefaultSSLSocketFactory(INSECURE_SSL_CONTEXT.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(INSECURE_HOSTNAME_VERIFIER);
    }

}
