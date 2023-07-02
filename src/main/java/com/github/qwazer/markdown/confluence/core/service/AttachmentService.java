package com.github.qwazer.markdown.confluence.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);

    private final ConfluenceService confluenceService;

    public AttachmentService(final ConfluenceService confluenceService) {
        this.confluenceService = confluenceService;
    }

    public void postAttachmentToPage(Long pageId, Path filePath) {

        LOG.info("Posting attachment {} to page {} in Confluence...", filePath.toString(), pageId);

        String attachmentId = confluenceService.getAttachmentId(pageId, filePath.getFileName().toString());
        if (attachmentId == null) {
            LOG.info("Create new attachment");
            confluenceService.createAttachment(pageId, filePath.toString());
        } else {
            LOG.info("Update existing attachment");
            confluenceService.updateAttachment(pageId, attachmentId, filePath.toString());
        }
    }
}
