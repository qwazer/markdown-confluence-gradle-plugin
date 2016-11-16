package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.UrlChecker;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.service.WikiToConfluenceService;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class WikiToConfluenceServiceImplIT {

    WikiToConfluenceService confluenceService;
    ConfluenceConfig confluenceConfig;


    @Before
    public void setUp() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        confluenceService = new WikiToConfluenceService(restTemplate);
        confluenceConfig = TestConfigFactory.testConfluenceConfig();
    }

    @Before
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getConfluenceRestApiUrl();
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 200));
    }

    @Test
    @Ignore
    public void testExistenseOfConfluence() throws Exception {
        confluenceService.postWikiToConfluence(confluenceConfig, "test");
    }

    @Test
    @Ignore
    public void testSimple() throws Exception {
        confluenceService.postWikiToConfluence(confluenceConfig, "h1.gradle-markdown-confluence\n" +
                "\n" +
                "Gradle plugin to publish markdown pages to confluence {code:java} java code;{code} _italic_");
    }


}