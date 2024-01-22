package com.github.qwazer.markdown.confluence.core;

import com.github.qwazer.markdown.confluence.core.model.ConfluenceSpace;
import com.github.qwazer.markdown.confluence.core.service.ConfluenceService;
import com.github.qwazer.markdown.confluence.gradle.plugin.AuthenticationType;
import okhttp3.OkHttpClient;
import org.junit.Before;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AbstractIT {

    public static final String CONFLUENCE_BASE_URL = "http://localhost:8090/rest/api";
    public static final String CONFLUENCE_SPACE_KEY = "SN";
    public static final AuthenticationType CONFLUENCE_AUTHENTICATION_TYPE = AuthenticationType.BASIC;
    public static final String CONFLUENCE_AUTHENTICATION =
        Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));

    protected ConfluenceService confluenceService;
    protected ConfluenceSpace confluenceSpace;

    @Before
    public void abstractBefore() {
        final String authorizationHeaderValue =
            CONFLUENCE_AUTHENTICATION_TYPE.getAuthorizationHeader(CONFLUENCE_AUTHENTICATION);
        final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(OkHttpUtils.getAuthorizationInterceptor(authorizationHeaderValue))
            .build();
        confluenceService = new ConfluenceService(CONFLUENCE_BASE_URL, CONFLUENCE_SPACE_KEY, httpClient);
        confluenceSpace = confluenceService.getOrCreateSpace(CONFLUENCE_SPACE_KEY);
    }

}
