package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.file.Path;

@Service
public class AttachmentService {

    private final ConfluenceService confluenceService;

    @Autowired
    public AttachmentService(final ConfluenceService confluenceService) {
        this.confluenceService = confluenceService;
    }

    public void postAttachmentToPage(Long pageId, Path filePath) {

        try {
            String attachmentId = confluenceService.getAttachmentId(pageId, filePath.getFileName().toString());
            if (attachmentId == null) {
                confluenceService.createAttachment(pageId, filePath.toString());
            } else {
                confluenceService.updateAttachment(pageId, attachmentId, filePath.toString());
            }
        } catch (HttpStatusCodeException e) {
            throw new ConfluenceException(e.getResponseBodyAsString(), e);
        }
    }
}
