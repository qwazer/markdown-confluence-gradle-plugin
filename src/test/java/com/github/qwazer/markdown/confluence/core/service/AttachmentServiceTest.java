package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AttachmentServiceTest {

    AttachmentService attachmentService;

    ConfluenceService confluenceService;

    @BeforeEach
    public void before() {
        confluenceService = mock(ConfluenceService.class);
        attachmentService = new AttachmentService(confluenceService);
    }

    @Test
    // Given confluenceService.getAttachmentId throws an HttpStatusCodeException
    // When postAttachmentToPage called
    // Then a ConfluenceException is thrown
    public void testGetAttachmentThrows() {
        given(confluenceService.getAttachmentId(anyLong(), anyString())).willThrow(mock(HttpServerErrorException.InternalServerError.class));

        Long expectedPageId = 1L;
        Path expectedPath = Paths.get("/a/path/file.png");

        assertThrows(ConfluenceException.class, () -> attachmentService.postAttachmentToPage(expectedPageId, expectedPath));

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