package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class AttachmentServiceTest {

    AttachmentService attachmentService;

    ConfluenceService confluenceService;

    @Before
    public void before() {
        confluenceService = mock(ConfluenceService.class);
        attachmentService = new AttachmentService(confluenceService);
    }

    @Test (expected = ConfluenceException.class)
    // Given confluenceService.getAttachmentId throws an HttpStatusCodeException
    // When postAttachmentToPage called
    // Then a ConfluenceException is thrown
    public void testGetAttachmentThrows() {
        given(confluenceService.getAttachmentId(anyLong(), anyString())).willThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, ""));

        Long expectedPageId = 1L;
        Path expectedPath = Paths.get("/a/path/file.png");

        attachmentService.postAttachmentToPage(expectedPageId, expectedPath);

        verifyZeroInteractions(confluenceService);
    }

    @Test
    // Given attachment does not exist
    // When postAttachmentToPage called
    // Then confluenceService.createAttachment is called
    public void testAttachmentDoesNotExist() {
        given(confluenceService.getAttachmentId(anyLong(), anyString())).willReturn(null);

        Long expectedPageId = 1L;
        Path expectedPath = Paths.get("/a/path/file.png");

        attachmentService.postAttachmentToPage(expectedPageId, expectedPath);

        verify(confluenceService).getAttachmentId(expectedPageId, expectedPath.getFileName().toString());
        verify(confluenceService).createAttachment(expectedPageId, expectedPath.toString());
        verifyNoMoreInteractions(confluenceService);
    }

    @Test
    // Given attachment does exist
    // When postAttachmentToPage called
    // Then confluenceService.updateAttachment is called
    public void testAttachmentDoesExist() {
        String attachmentId = "123";

        given(confluenceService.getAttachmentId(anyLong(), anyString())).willReturn(attachmentId);

        Long expectedPageId = 1L;
        Path expectedPath = Paths.get("/a/path/file.png");

        attachmentService.postAttachmentToPage(expectedPageId, expectedPath);

        verify(confluenceService).getAttachmentId(expectedPageId, expectedPath.getFileName().toString());
        verify(confluenceService).updateAttachment(expectedPageId, attachmentId, expectedPath.toString());
        verifyNoMoreInteractions(confluenceService);
    }
}