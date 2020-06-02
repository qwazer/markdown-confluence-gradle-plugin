package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.*;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.Assert.*;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class PageServiceIT {

    @Autowired
    private PageService pageService;
    @Autowired
    private ConfluenceService confluenceService;
    private final ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
    private final ConfluenceConfig.Page page = TestConfigFactory.getPage();


    @Before
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getRestApiUrl();
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 500));
        confluenceService.setConfluenceConfig(confluenceConfig);
    }

    @Test
    @Ignore
    public void testExistenseOfConfluence() throws Exception {
        pageService.postWikiPageToConfluence(page, "test");
    }

    @Test
    @Ignore
    public void testSimple() throws Exception {
        pageService.postWikiPageToConfluence(page, "h1.gradle-markdown-confluence\n" +
                "\n" +
                "Gradle plugin to publish markdown pages to confluence {code:java} java code;{code} _italic_");
    }


    @Test
    public void testTryToCreateErroredPage() throws Exception {

        confluenceService.setConfluenceConfig(confluenceConfig);
        ConfluenceConfig.Page page = new ConfluenceConfig.Page();
        page.setTitle("temp");
        page.setParentTitle("HOME");

        try {
            pageService.postWikiPageToConfluence(page, "{no_such_macros}");
        } catch (ConfluenceException e) {
            assertTrue(e.getMessage().contains("The macro \'no_such_macros\' is unknown"));
            return;
        }
        fail();
    }
}