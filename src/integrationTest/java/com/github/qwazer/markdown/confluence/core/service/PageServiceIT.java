package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.AbstractIT;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.gradle.plugin.ConfluenceExtension;
import kotlin.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class PageServiceIT extends AbstractIT {

    private PageService pageService;

    @Before
    public void before() {
        pageService = new PageService(confluenceService);
    }

    @Test
    public void testPrepareWikiTextWithInlineImage() throws IOException {

        ConfluenceExtension.Page page = Mockito.mock();
        Mockito.when(page.getName()).thenReturn("Page with invalid macro");
        Mockito.when(page.getTitle()).thenCallRealMethod();
        Mockito.when(page.getParentTitle()).thenReturn("Home");
        Mockito.when(page.getSrcFile()).thenReturn(new File("README.md"));
        final String markdown = "# Page with inline image\n\nThis is an image: ![Cool Picture](pics/picture.jpg \"Cool Picture's Title\")";
        Mockito.when(page.getContent()).thenReturn(markdown);

        final Pair<String, Map<String, Path>> pair = pageService.prepareWikiText(page);
        final String wikiText = pair.getFirst();
        final Map<String, Path> attachments = pair.getSecond();

        assertTrue(wikiText.contains("!picture.jpg|thumbnail, alt=\"Cool Picture\", title=\"Cool Picture's Title\"!"));
        assertFalse(attachments.isEmpty());
    }

    @Test
    public void testPublishingPageWithUnknownMacroShouldFail() throws IOException {

        ConfluenceExtension.Page page = Mockito.mock();
        Mockito.when(page.getName()).thenReturn("Page with invalid macro");
        Mockito.when(page.getTitle()).thenCallRealMethod();
        Mockito.when(page.getParentTitle()).thenReturn("Home");
        Mockito.when(page.getContent()).thenReturn("{no_such_macro}");

        try {
            pageService.publishWikiPage(page);
        } catch (ConfluenceException e) {
            assertTrue(e.getMessage().contains("The macro 'no_such_macro' is unknown"));
            return;
        }
        fail();
    }
}