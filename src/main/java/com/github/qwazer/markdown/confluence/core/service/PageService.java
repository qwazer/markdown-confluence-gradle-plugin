package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

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

        try {
            ConfluencePage oldPage = confluenceService.findPageByTitle(page.getTitle());

            if (oldPage != null) {               // page exists
                LOG.info("Update existing page");
                oldPage.setContent(wiki);
                oldPage.setTitle(page.getTitle());
                oldPage.setLabels(page.getLabels());
                confluenceService.updatePage(oldPage);
                confluenceService.addLabels(oldPage.getId(), page.getLabels());

            } else {
                LOG.info("Create new page");
                ConfluencePage newPage = new ConfluencePage();
                newPage.setContent(wiki);
                newPage.setTitle(page.getTitle());
                newPage.setLabels(page.getLabels());
                Long ancestorId = confluenceService.findAncestorId(page.getParentTitle());
                newPage.setAncestorId(ancestorId);

                Long pageId = confluenceService.createPage(newPage);

                confluenceService.addLabels(pageId, page.getLabels());
            }
        } catch (HttpStatusCodeException e) {
            throw new ConfluenceException(e.getResponseBodyAsString(), e);
        }

    }


    public <T> List<T> union(Collection<T> list1, Collection<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    public <T> List<T> intersection(Collection<T> list1, Collection<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }


}