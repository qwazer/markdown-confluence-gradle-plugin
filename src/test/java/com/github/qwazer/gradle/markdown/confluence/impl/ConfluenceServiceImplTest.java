package com.github.qwazer.gradle.markdown.confluence.impl;

import com.github.qwazer.gradle.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.gradle.markdown.confluence.core.ConfluenceConfigTestFactory;
import com.github.qwazer.gradle.markdown.confluence.service.ConfluenceService;
import com.github.qwazer.gradle.markdown.confluence.service.Markdown2WikiService;
import com.github.qwazer.gradle.markdown.confluence.service.Wiki2ConfluenceService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class ConfluenceServiceImplTest {

    private ConfluenceService confluenceService;
    ConfluenceConfig confluenceConfig;

    @Before
    public void setUp() throws Exception {
        confluenceConfig = ConfluenceConfigTestFactory.testConfluenceConfig();


        RestTemplate restTemplate = new RestTemplate();
        Wiki2ConfluenceService wikiToConfluenceService = new WikiToConfluenceServiceImpl(restTemplate);
        Markdown2WikiService markdown2WikiService = new Markdown2WikiServiceImpl();
        confluenceService = new ConfluenceServiceImpl(
                new FileReaderServiceImpl(),
                markdown2WikiService,
                wikiToConfluenceService);

    }

    @Test
    public void processAll() throws Exception {
        confluenceService.processAll(confluenceConfig);
    }

}