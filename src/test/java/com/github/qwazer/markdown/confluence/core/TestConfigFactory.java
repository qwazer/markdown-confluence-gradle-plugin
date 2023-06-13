package com.github.qwazer.markdown.confluence.core;

import org.gradle.internal.impldep.org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class TestConfigFactory {

    public static ConfluenceConfig.Page getPage(){
        ConfluenceConfig.Page page = new ConfluenceConfig.Page();
        page.setSrcFile(new File("README.md"));
        page.setTitle("README.md");
        return page;
    }

    public static  ConfluenceConfig testConfluenceConfig(){
        ConfluenceConfig config = new ConfluenceConfig();

        config.setSpaceKey("SN");
        config.setRestApiUrl("http://localhost:8090/rest/api/");
        config.setAuthentication(getAuth());
        config.setPages(new ArrayList<ConfluenceConfig.Page>());
        config.getPages().add(getPage());
        return  config;
    }

    public static ConfluenceConfig testPatConfluenceConfig() {
        ConfluenceConfig config = new ConfluenceConfig();

        config.setSpaceKey("SN");
        config.setRestApiUrl("http://localhost:8090/rest/api/");
        config.setAuthenticationType(ConfluenceConfig.AuthenticationType.PAT);
        config.setAuthentication("token");
        config.setPages(new ArrayList<>());
        config.getPages().add(getPage());
        return  config;
    }

    public static String getAuth() {
        final String plainCreds = "admin:admin";
        final byte[] plainCredsBytes = plainCreds.getBytes();
        final byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        return new String(base64CredsBytes);
    }
}