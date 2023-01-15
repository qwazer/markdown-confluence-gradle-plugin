package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringConfig.class)
public class PageServiceIT {

    @Autowired
    private PageService pageService;
    @Autowired
    private ConfluenceService confluenceService;
    private final ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
    private final ConfluenceConfig.Page page = TestConfigFactory.getPage();


    @BeforeEach
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getRestApiUrl();
        assertTrue(UrlChecker.pingConfluence(url, 500), "Url should be available " + url );
        confluenceService.setConfluenceConfig(confluenceConfig);
    }

    @Test
    @Disabled
    public void testExistenseOfConfluence() throws Exception {
        pageService.postWikiPageToConfluence(page, "test");
    }

    @Test
    @Disabled
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

        final Exception actualError = assertThrows(ConfluenceException.class, ()->pageService.postWikiPageToConfluence(page, "{no_such_macros}"));

        assertThat(actualError.getMessage(), Matchers.containsString("The macro 'no_such_macros' is unknown"));
    }
}