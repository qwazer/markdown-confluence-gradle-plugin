package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentServiceTest {

    @Mock
    private ConfluenceService confluenceService;
    private AttachmentService attachmentService;


    @Before
    public void before() {
        attachmentService = new AttachmentService(confluenceService);
    }

    @Test (expected = ConfluenceException.class)
    // Given confluenceService.getAttachmentId throws an HttpStatusCodeException
    // When postAttachmentToPage called
    // Then a ConfluenceException is thrown
    public void testGetAttachmentThrows() {
        given(confluenceService.getAttachmentId(anyLong(), anyString()))
            .willThrow(new ConfluenceException("Controlled exception"));

        final Long expectedPageId = 1L;
        final Path expectedPath = Paths.get("/a/path/file.png");

        attachmentService.postAttachmentToPage(expectedPageId, expectedPath);

        verifyNoInteractions(confluenceService);
    }

    @Test
    // Given attachment does not exist
    // When postAttachmentToPage called
    // Then confluenceService.createAttachment is called
    public void testAttachmentDoesNotExist() {
        given(confluenceService.getAttachmentId(anyLong(), anyString())).willReturn(null);

        final Long expectedPageId = 1L;
        final Path expectedPath = Paths.get("/a/path/file.png");

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
        final String attachmentId = "123";

        given(confluenceService.getAttachmentId(anyLong(), anyString())).willReturn(attachmentId);

        final Long expectedPageId = 1L;
        final Path expectedPath = Paths.get("/a/path/file.png");

        attachmentService.postAttachmentToPage(expectedPageId, expectedPath);

        verify(confluenceService).getAttachmentId(expectedPageId, expectedPath.getFileName().toString());
        verify(confluenceService).updateAttachment(expectedPageId, attachmentId, expectedPath.toString());
        verifyNoMoreInteractions(confluenceService);
    }
}