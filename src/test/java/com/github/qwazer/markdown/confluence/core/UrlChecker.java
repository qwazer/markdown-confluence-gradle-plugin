package com.github.qwazer.markdown.confluence.core;

import com.github.qwazer.markdown.confluence.core.ssl.SslUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Anton Reshetnikov on 16 Nov 2016.
 */
public class UrlChecker {

    public static boolean pingConfluence(String baseUrl, int timeout) {

        if (!baseUrl.endsWith("/")){
            baseUrl = baseUrl + "/";
        }
        baseUrl = baseUrl + "user/current";


        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }
}
