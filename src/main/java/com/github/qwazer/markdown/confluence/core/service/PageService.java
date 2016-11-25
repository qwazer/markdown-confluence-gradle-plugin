package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Anton Reshetnikov on 14 Nov 2016.
 */
@Service
public class PageService {

    private static final Logger LOG = LoggerFactory.getLogger(PageService.class);


    private final ConfluenceService confluenceService;

    @Autowired
    public PageService(final ConfluenceService confluenceService) {
        this.confluenceService = confluenceService;
    }

    public void postWikiPageToConfluence(final ConfluenceConfig.Page page, final ConfluenceConfig confluenceConfig, final String wiki) {
        LOG.info("Posting page {} to Confluence...", page.getTitle());

        confluenceService.setConfluenceConfig(confluenceConfig);

        ConfluencePage oldPage = confluenceService.findPageByTitle(page.getTitle());

        if (oldPage != null) {               // page exists
            LOG.info("Update existing page");
            oldPage.setContent(wiki);
            oldPage.setConfluenceTitle(page.getTitle());
            confluenceService.updatePage(oldPage);

        } else {
            LOG.info("Create new page");
            ConfluencePage newPage = new ConfluencePage();
            newPage.setContent(wiki);
            newPage.setConfluenceTitle(page.getTitle());
            Long ancestorId = confluenceService.findAncestorId(page.getParentTitle());
            newPage.setAncestorId(ancestorId);

            confluenceService.createPage(newPage);
        }

    }


}