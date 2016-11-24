package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class WikiToConfluenceServiceImplIT {

    @Autowired
    private WikiToConfluenceService confluenceService;
    private final ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
    private final ConfluenceConfig.Page page = TestConfigFactory.getPage();


    @Before
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getConfluenceRestApiUrl();
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 200));
    }

    @Test
    @Ignore
    public void testExistenseOfConfluence() throws Exception {

        confluenceService.postWikiPageToConfluence(page, confluenceConfig, "test");
    }

    @Test
    @Ignore
    public void testSimple() throws Exception {
        confluenceService.postWikiPageToConfluence(page, confluenceConfig, "h1.gradle-markdown-confluence\n" +
                "\n" +
                "Gradle plugin to publish markdown pages to confluence {code:java} java code;{code} _italic_");
    }


    @Test
    public void testFindAncestorId() throws Exception {
        confluenceConfig.setParentPage("SN+Home");
        Long id = confluenceService.findAncestorId(confluenceConfig);
        assertNotNull(id);

    }
}