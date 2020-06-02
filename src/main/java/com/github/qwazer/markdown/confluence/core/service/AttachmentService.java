package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.file.Path;

@Service
public class AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);

    private final ConfluenceService confluenceService;

    @Autowired
    public AttachmentService(final ConfluenceService confluenceService) {
        this.confluenceService = confluenceService;
    }

    public void postAttachmentToPage(Long pageId, Path filePath) {

        LOG.info("Posting attachment {} to page {} in Confluence...", filePath.toString(), pageId);

        try {
            String attachmentId = confluenceService.getAttachmentId(pageId, filePath.getFileName().toString());
            if (attachmentId == null) {
                LOG.info("Create new attachment");
                confluenceService.createAttachment(pageId, filePath.toString());
            } else {
                LOG.info("Update existing attachment");
                confluenceService.updateAttachment(pageId, attachmentId, filePath.toString());
            }
        } catch (HttpStatusCodeException e) {
            LOG.error("Error creating/updating attachment.", e);
            throw new ConfluenceException(e.getResponseBodyAsString(), e);
        }
    }
}
