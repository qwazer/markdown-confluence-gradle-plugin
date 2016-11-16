package com.github.qwazer.markdown.confluence.core;

import org.gradle.internal.impldep.org.apache.commons.codec.binary.Base64;

import java.io.File;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class TestConfigFactory {


    public static  ConfluenceConfig testConfluenceConfig(){
        ConfluenceConfig config = new ConfluenceConfig();

        config.setSpaceKey("SN");
        config.setConfluenceRestApiUrl("http://localhost:8090/rest/api/");
        config.setAuthentication(getAuth());
        config.setTitle("README.md");
        config.setBaseFile(new File("README.md"));
        return  config;
    }

    private static String getAuth() {
        final String plainCreds = "admin:admin";
        final byte[] plainCredsBytes = plainCreds.getBytes();
        final byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        return new String(base64CredsBytes);
    }

}