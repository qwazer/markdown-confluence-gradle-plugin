package com.github.qwazer.markdown.confluence.core.service.impl;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfigTestFactory;
import com.github.qwazer.markdown.confluence.core.service.ConfluenceService;
import com.github.qwazer.markdown.confluence.core.service.Markdown2WikiService;
import com.github.qwazer.markdown.confluence.core.service.Wiki2ConfluenceService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

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