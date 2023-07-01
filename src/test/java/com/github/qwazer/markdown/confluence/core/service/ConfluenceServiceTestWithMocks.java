package com.github.qwazer.markdown.confluence.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.plugins.MockMaker;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ConfluenceServiceTestWithMocks {

    ConfluenceService confluenceService;

    RestTemplate restTemplate;

    @Before
    public void before() {
        restTemplate = mock(RestTemplate.class);
        confluenceService = new ConfluenceService(restTemplate);
        ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
        confluenceService.setConfluenceConfig(confluenceConfig);
    }

    @Test
    // Given a call to confluence returns no attachment
    // When getAttachmentId called
    // Then the returned attachmentId is correct
    public void testGetAttachmentId_attachment_does_not_exist() {
        final var responseBody = new ObjectNode(JsonNodeFactory.instance);
        responseBody.set("results", new ArrayNode(JsonNodeFactory.instance));

        ResponseEntity<JsonNode> responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getBody()).willReturn(responseBody);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class)))
            .willReturn(responseEntity);

        Long pageId = 1L;
        String attachmentFilename = "file.png";

        String actualAttachmentId = confluenceService.getAttachmentId(pageId, attachmentFilename);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restTemplate)
            .exchange(captor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class));
        verifyNoMoreInteractions(restTemplate);

        URI actualURI = captor.getValue();
        assertEquals(String.format("/rest/api/content/%d/child/attachment", pageId), actualURI.getPath());
        assertEquals(String.format("filename=%s", attachmentFilename), actualURI.getQuery());

        assertNull(actualAttachmentId);
    }

    @Test
    // Given a call to confluence returns valid json with attachment
    // When getAttachmentId called
    // Then the returned attachmentId is correct
    public void testGetAttachmentId_attachment_exists() {
        String expectedAttachmentId = "1234";
//        String expectedBody = String.format("{ \"results\": [ { \"id\": \"%s\" } ] }", expectedAttachmentId);
        final var expectedBody = new ObjectNode(JsonNodeFactory.instance)
            .set(
                "results",
                new ArrayNode(JsonNodeFactory.instance)
                    .add(
                        new ObjectNode(JsonNodeFactory.instance)
                            .put("id", expectedAttachmentId)
                    )
            );
        System.out.println(expectedBody.toString());

        ResponseEntity<JsonNode> responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getBody()).willReturn(expectedBody);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class)))
            .willReturn(responseEntity);

        Long pageId = 1L;
        String attachmentFilename = "file.png";

        String actualAttachmentId =
            confluenceService.getAttachmentId(pageId, attachmentFilename);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restTemplate).exchange(captor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class));
        verifyNoMoreInteractions(restTemplate);

        URI actualURI = captor.getValue();
        assertEquals(String.format("/rest/api/content/%d/child/attachment", pageId), actualURI.getPath());
        assertEquals(String.format("filename=%s", attachmentFilename), actualURI.getQuery());

        assertEquals(expectedAttachmentId, actualAttachmentId);
    }

    @Test
    // Given a call to confluence returns with no error
    // When createAttachment called
    // Then the correct call to confluence is made
    public void testCreateAttachment() {
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getBody()).willReturn("{}"); // Don't check what is returned so just return an empty json object
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).willReturn((ResponseEntity<String>) responseEntity);

        Long pageId = 1L;
        String attachmentFilename = "file.png";

        confluenceService.createAttachment(pageId, attachmentFilename);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity> captorForHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(captor.capture(), eq(HttpMethod.POST), captorForHttpEntity.capture(), eq(String.class));
        verifyNoMoreInteractions(restTemplate);

        URI actualURI = captor.getValue();
        assertEquals(String.format("/rest/api/content/%d/child/attachment", pageId), actualURI.getPath());

        HttpEntity actualHttpEntity = captorForHttpEntity.getValue();

        FileSystemResource file = (FileSystemResource) ((LinkedMultiValueMap) actualHttpEntity.getBody()).get("file").get(0);
        String name = file.getFile().getName();
        assertEquals(name, attachmentFilename);
    }

    @Test
    // Given a call to confluence returns with no error
    // When updateAttachment called
    // Then the correct call to confluence is made
    public void testUpdateAttachment() {
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getBody()).willReturn("{}"); // Don't check what is returned so just return an empty json object
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class))).willReturn((ResponseEntity<String>) responseEntity);

        Long pageId = 1L;
        String attachmentFilename = "file.png";
        String attachmentId = "4321";

        confluenceService.updateAttachment(pageId, attachmentId, attachmentFilename);

        ArgumentCaptor<URI> captorForURI = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity> captorForHttpEntity = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(captorForURI.capture(), eq(HttpMethod.POST), captorForHttpEntity.capture(), eq(String.class));
        verifyNoMoreInteractions(restTemplate);

        URI actualURI = captorForURI.getValue();
        assertEquals(String.format("/rest/api/content/%d/child/attachment/%s/data", pageId, attachmentId), actualURI.getPath());

        HttpEntity actualHttpEntity = captorForHttpEntity.getValue();

        FileSystemResource file = (FileSystemResource) ((LinkedMultiValueMap) actualHttpEntity.getBody()).get("file").get(0);
        String name = file.getFile().getName();
        assertEquals(name, attachmentFilename);
    }

    @Test
    public void testAuthorizationHeaderWhenBasicAuthenticationIsUsed() {
        assertEquals(String.format("Basic %s", TestConfigFactory.getAuth()), confluenceService.getHttpHeaders().get("Authorization").get(0));
    }

    @Test
    public void testAuthorizationHeaderWhenPatAuthenticationIsUsed() {
        confluenceService.setConfluenceConfig(TestConfigFactory.testPatConfluenceConfig());
        assertEquals("Bearer token", confluenceService.getHttpHeaders().get("Authorization").get(0));
    }

}
