package com.github.qwazer.markdown.confluence.core.service.impl;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfigTestFactory;
import com.github.qwazer.markdown.confluence.core.service.Wiki2ConfluenceService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class WikiToConfluenceServiceImplIT {

    Wiki2ConfluenceService confluenceService;
    ConfluenceConfig confluenceConfig;


    @Before
    public void setUp() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        confluenceService = new WikiToConfluenceServiceImpl(restTemplate);
        confluenceConfig = ConfluenceConfigTestFactory.testConfluenceConfig();
    }

    @Test
    public void testExistenseOfConfluence() throws Exception {
        confluenceService.postWikiToConfluence(confluenceConfig, "test");
    }

    @Test
    public void testSimple() throws Exception {
        confluenceService.postWikiToConfluence(confluenceConfig, "h1.gradle-markdown-confluence\n" +
                "\n" +
                "Gradle plugin to publish markdown pages to confluence {code:java} java code;{code} _italic_");
    }


}