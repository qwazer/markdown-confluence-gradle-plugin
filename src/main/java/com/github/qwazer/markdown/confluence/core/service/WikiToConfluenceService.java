package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static org.jsoup.nodes.Entities.EscapeMode.xhtml;

/**
 * Created by Anton Reshetnikov on 14 Nov 2016.
 */
@Service
public class WikiToConfluenceService {

    private static final Logger LOG = LoggerFactory.getLogger(WikiToConfluenceService.class);


    private final ConfluenceService confluenceService;

    @Autowired
    public WikiToConfluenceService(final ConfluenceService confluenceService) {
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
            Long ancestorId = confluenceService.findAncestorId(page.getParentPage());
            newPage.setAncestorId(ancestorId);

            confluenceService.createPage(newPage);
        }

    }


}