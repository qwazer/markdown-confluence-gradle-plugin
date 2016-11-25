package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class ConfluenceServiceTest {

    @Autowired
    private ConfluenceService confluenceService;
    private final ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
    private final ConfluenceConfig.Page page = TestConfigFactory.getPage();

    @Before
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getRestApiUrl();
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 500));
    }

    @Test
    public void testFindAncestorId() throws Exception {
        confluenceService.setConfluenceConfig(confluenceConfig);
        Long id = confluenceService.findAncestorId(confluenceConfig.getSpaceKey());
        assertNotNull(id);

    }

    @Test
    public void testTryToCreateErroredPage() throws Exception {

        confluenceService.setConfluenceConfig(confluenceConfig);
        ConfluencePage page = new ConfluencePage();
        page.setConfluenceTitle("temp");
        page.setContent("{no_such_macros}");
        Long id = confluenceService.findAncestorId(confluenceConfig.getSpaceKey());
        page.setAncestorId(id);


        confluenceService.createPage(page);

    }
}