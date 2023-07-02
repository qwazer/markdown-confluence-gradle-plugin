package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.OkHttpUtils;
import com.github.qwazer.markdown.confluence.gradle.plugin.AuthenticationType;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfluenceServiceTest {

    private MockWebServer webServer;
    private ConfluenceService confluenceService;

    @Before
    public void before() throws Exception {
        webServer = new MockWebServer();
        webServer.start();
        final String restApiUrl =
            String.format("http://localhost:%d/rest/api/", webServer.getPort());
        final String authorization =
            AuthenticationType.PAT.getAuthorizationHeader("token");
        final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(OkHttpUtils.getAuthorizationInterceptor(authorization))
            .build();
        confluenceService = new ConfluenceService(restApiUrl, "SN", httpClient);
    }

    @After
    public void after() throws Exception {
        webServer.shutdown();
    }

    // Given a call to confluence returns no attachment
    // When getAttachmentId called
    // Then the returned attachmentId is correct
    @Test
    public void testGetAttachmentId_attachment_does_not_exist() throws InterruptedException {

        final Long pageId = 1L;
        final String attachmentFilename = "file.png";
        // the /rest/api part of the path comes from the base URI configured for testing
        final String happyPath =
            String.format("/rest/api/content/%d/child/attachment?filename=%s", pageId, attachmentFilename);

        webServer.setDispatcher(getDispatcher(happyPath, "{ \"results\": [] }"));

        // when
        final String actualAttachmentId = confluenceService.getAttachmentId(pageId, attachmentFilename);

        // then
        final RecordedRequest recordedRequest = webServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        final HttpUrl httpUrl = recordedRequest.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals(attachmentFilename, httpUrl.queryParameter("filename"));

        assertNull(actualAttachmentId);
    }

    // Given a call to confluence returns valid json with attachment
    // When getAttachmentId called
    // Then the returned attachmentId is correct
    @Test
    public void testGetAttachmentId_attachment_exists() throws InterruptedException {

        final Long pageId = 1L;
        final String attachmentFilename = "file.png";
        final String expectedAttachmentId = "1234";
        // the /rest/api part of the path comes from the base URI configured for testing
        final String expectedPath =
            String.format("/rest/api/content/%d/child/attachment?filename=%s", pageId, attachmentFilename);
        final String expectedBody =
            String.format("{ \"results\": [ { \"id\": \"%s\" } ] }", expectedAttachmentId);

        webServer.setDispatcher(getDispatcher(expectedPath, expectedBody));

        final String actualAttachmentId = confluenceService.getAttachmentId(pageId, attachmentFilename);

        // then
        final RecordedRequest recordedRequest = webServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        final HttpUrl httpUrl = recordedRequest.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals(attachmentFilename, httpUrl.queryParameter("filename"));

        assertEquals(expectedAttachmentId, actualAttachmentId);
    }

    // Given a call to confluence returns with no error
    // When createAttachment called
    // Then the correct call to confluence is made
    @Test
    public void testCreateAttachment() throws Exception {

        final String happyPath = "/rest/api/content/1/child/attachment";
        webServer.setDispatcher(getDispatcher(happyPath, "{}"));

        // when
        final Long pageId = 1L;

        final URL attachmentUrl = getClass().getResource("/test.md");
        assertNotNull(attachmentUrl);
        final Path attachmentFilePath = Paths.get(attachmentUrl.toURI());

        confluenceService.createAttachment(pageId, attachmentFilePath.toString());

        // then
        final RecordedRequest recordedRequest = webServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        final HttpUrl httpUrl = recordedRequest.getRequestUrl();
        assertNotNull(httpUrl);

        // this is due to the provided ConfluenceConfig
        assertEquals("Bearer token", recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
        final String contentType = recordedRequest.getHeader("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("multipart/form-data; boundary="));

    }

    @Test
    // Given a call to confluence returns with no error
    // When updateAttachment called
    // Then the correct call to confluence is made
    public void testUpdateAttachment() throws Exception {

        final String happyPath = "/rest/api/content/1/child/attachment/4321/data";
        webServer.setDispatcher(getDispatcher(happyPath, "{}"));

        // when
        final Long pageId = 1L;
        final String attachmentId = "4321";
        final URL attachmentUrl = getClass().getResource("/test.md");
        assertNotNull(attachmentUrl);
        final Path attachmentFilePath = Paths.get(attachmentUrl.toURI());

        confluenceService.updateAttachment(pageId, attachmentId, attachmentFilePath.toString());

        // then
        final RecordedRequest recordedRequest = webServer.takeRequest(3, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        final HttpUrl httpUrl = recordedRequest.getRequestUrl();
        assertNotNull(httpUrl);

        // this is due to the provided ConfluenceConfig
        assertEquals("Bearer token", recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
        final String contentType = recordedRequest.getHeader("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("multipart/form-data; boundary="));

    }

    /**
     * @param happyPath - request path for which to respond with HTTP 200 - OK and {@code happyBody}
     * @param happyBody - the body of the HTTP 200 - OK response.
     * @return A {@link Dispatcher} that responds with HTTP 200 - OK when the request is made to the {@code happyPath}.
     * The payload of the returned response is {@code happyBody}. If the request path does not match {@code happyPath}
     * then HTTP 404 response with no payload is returned.
     */
    private Dispatcher getDispatcher(@NotNull final String happyPath, @NotNull final String happyBody) {
        return new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) {
                if (recordedRequest.getHeader("Authorization") == null) {
                    return new MockResponse().setResponseCode(401).setBody("UNAUTHORIZED");
                }
                if (happyPath.equals(recordedRequest.getPath())) {
                    return new MockResponse().setResponseCode(200).setBody(happyBody);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
    }

}
