package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class ConfluenceServiceTestWithMocks {

    ConfluenceService confluenceService;

    RestTemplate restTemplate;

    @BeforeEach
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
        String expectedBody = "{ \"results\": [] }";

        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getBody()).willReturn(expectedBody);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).willReturn((ResponseEntity<String>) responseEntity);

        Long pageId = 1L;
        String attachmentFilename = "file.png";

        String actualAttachmentId = confluenceService.getAttachmentId(pageId, attachmentFilename);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restTemplate).exchange(captor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        verifyNoMoreInteractions(restTemplate);

        URI actualURI = captor.getValue();
        assertEquals(String.format("/rest/api/content/%d/child/attachment", pageId), actualURI.getPath());
        assertEquals(String.format("filename=%s", attachmentFilename), actualURI.getQuery());

        assertEquals(null, actualAttachmentId);
    }

    @Test
    // Given a call to confluence returns valid json with attachment
    // When getAttachmentId called
    // Then the returned attachmentId is correct
    public void testGetAttachmentId_attachment_exists() {
        String expectedAttachmentId = "1234";
        String expectedBody = String.format("{ \"results\": [ { \"id\": \"%s\" } ] }", expectedAttachmentId);

        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        given(responseEntity.getBody()).willReturn(expectedBody);
        given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).willReturn((ResponseEntity<String>) responseEntity);

        Long pageId = 1L;
        String attachmentFilename = "file.png";

        String actualAttachmentId = confluenceService.getAttachmentId(pageId, attachmentFilename);

        ArgumentCaptor<URI> captor = ArgumentCaptor.forClass(URI.class);

        verify(restTemplate).exchange(captor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
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
}
