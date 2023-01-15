package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.gradle.internal.impldep.org.hamcrest.MatcherAssert.assertThat;
import static org.gradle.internal.impldep.org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringConfig.class)
public class ConfluenceServiceTest {

    @Autowired
    private ConfluenceService confluenceService;
    private final ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
    private final ConfluenceConfig.Page page = TestConfigFactory.getPage();

    @BeforeEach
    public void pingRestAPIUrl() {
        String url = TestConfigFactory.testConfluenceConfig().getRestApiUrl();
        assertTrue(UrlChecker.pingConfluence(url, 500), "Url should be available " + url);
    }

    @BeforeEach
    public void setUp() throws Exception {
        confluenceService.setConfluenceConfig(confluenceConfig);
    }

    @Test
    public void testFindAncestorId() throws Exception {
        confluenceService.setConfluenceConfig(confluenceConfig);
        Long id = confluenceService.findAncestorId(confluenceConfig.getSpaceKey());
        assertThat(id, notNullValue());
    }


    @Test
    public void testCreatePage() throws Exception {
        ConfluencePage page = new ConfluencePage();
        page.setTitle("temp-" + UUID.randomUUID().toString());
        page.setContent("hello");
        confluenceService.createPage(page);
    }


    @Test
    @Disabled
    public void testCreatePageWithUnkownMacro() throws Exception {
        ConfluencePage page = new ConfluencePage();
        page.setTitle("temp-" + UUID.randomUUID().toString());
        page.setContent("{unknown_macro} hello");
        confluenceService.createPage(page);
    }


    @Test
    public void testAddLabel() throws Exception {
        ConfluencePage page = new ConfluencePage();
        page.setTitle("temp-" + UUID.randomUUID().toString());
        page.setContent("hello");
        Long pageId = confluenceService.createPage(page);

        confluenceService.addLabels(pageId, Arrays.asList("l1", "l2"));

    }


}