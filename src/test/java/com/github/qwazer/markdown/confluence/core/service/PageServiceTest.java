package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.gradle.plugin.ConfluenceExtension;
import kotlin.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class PageServiceTest {

    @Test
    public void markdownReferringToExistingLocalImageShouldBeCorrectlyConverted() throws Exception {

        ConfluenceService confluenceService = Mockito.mock(ConfluenceService.class);
        PageService pageService = new PageService(confluenceService);
        ConfluenceExtension.Page page = Mockito.mock(ConfluenceExtension.Page.class);
        Mockito.when(page.getName()).thenReturn("Test Page");
        Mockito.when(page.getTitle()).thenReturn("Test Page");
        Mockito.when(page.getParentTitle()).thenReturn("Parent Page");
        URL testResource = getClass().getResource("PageServiceTest1.md");
        Assert.assertNotNull(testResource);
        File markdownFile = new File(testResource.toURI());
        Mockito.when(page.getSrcFile()).thenReturn(markdownFile);
        Mockito.when(page.getContent()).thenCallRealMethod();

        Pair<String, List<Path>> result = pageService.prepareWikiText(page);
        String confluenceWiki = result.getFirst();
        List<Path> attachments = result.getSecond();
        Assert.assertEquals("!image.png|Image Description!", confluenceWiki.trim());
        Assert.assertEquals("image.png", attachments.get(0).getFileName().toString());

    }

    @Test(expected = ConfluenceException.class)
    public void markdownReferringToNonExistentLocalImageShouldResultInException() throws Exception {

        ConfluenceService confluenceService = Mockito.mock(ConfluenceService.class);
        PageService pageService = new PageService(confluenceService);
        ConfluenceExtension.Page page = Mockito.mock(ConfluenceExtension.Page.class);
        Mockito.when(page.getName()).thenReturn("Test Page");
        Mockito.when(page.getTitle()).thenReturn("Test Page");
        Mockito.when(page.getParentTitle()).thenReturn("Parent Page");
        URL testResource = getClass().getResource("PageServiceTest2.md");
        Assert.assertNotNull(testResource);
        File markdownFile = new File(testResource.toURI());
        Mockito.when(page.getSrcFile()).thenReturn(markdownFile);
        Mockito.when(page.getContent()).thenCallRealMethod();

        pageService.prepareWikiText(page);

    }

}