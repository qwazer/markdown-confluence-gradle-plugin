package com.github.qwazer.gradle.markdown.confluence.core;

import org.gradle.internal.impldep.org.apache.commons.codec.binary.Base64;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class ConfluenceConfigTestFactory {


    public static  ConfluenceConfig testConfluenceConfig(){
        ConfluenceConfig config = new ConfluenceConfig();

        config.setAncestorId(0L);
        config.setSpaceKey("SN");
        config.setConfluenceRestApiUrl("http://localhost:8090/rest/api/");
        config.setAuthentication(getAuth());
        return  config;
    }

    private static String getAuth() {
        final String plainCreds = "admin:admin";
        final byte[] plainCredsBytes = plainCreds.getBytes();
        final byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        return new String(base64CredsBytes);
    }

}