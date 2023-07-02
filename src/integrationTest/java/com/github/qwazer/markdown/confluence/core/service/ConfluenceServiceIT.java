package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.AbstractIT;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
public class ConfluenceServiceIT extends AbstractIT {

    @Test
    public void testFindAncestorId() {
        final Long id = confluenceService.findAncestorId(confluenceSpace.getKey());
        assertNotNull(id);
    }

    @Test
    public void testCreatePage() {
        final ConfluencePage page = new ConfluencePage();
        page.setTitle("Basic Test Page - " + UUID.randomUUID());
        page.setContent("Hello, Confluence!");
        confluenceService.createPage(page);

        final ConfluencePage createdPage = confluenceService.findPageByTitle(page.getTitle());
        assertNotNull(createdPage);
        assertTrue(createdPage.getContent().contains(page.getContent()));
    }

    @Test
    public void testUpdatePage() {

        final ConfluencePage initialPage = new ConfluencePage();
        initialPage.setTitle("Update Page Test - " + UUID.randomUUID());
        initialPage.setContent("Hello, Confluence!");
        confluenceService.createPage(initialPage);

        final ConfluencePage savedPage = confluenceService.findPageByTitle(initialPage.getTitle());
        assertNotNull(savedPage);

        final ConfluencePage updatedPage = new ConfluencePage();
        updatedPage.setId(savedPage.getId());
        updatedPage.setTitle(savedPage.getTitle());
        updatedPage.setContent("Hello, Confluence after update!");
        updatedPage.setLabels(savedPage.getLabels());
        updatedPage.setVersion(savedPage.getVersion());
        confluenceService.updatePage(updatedPage);

        final ConfluencePage savedUpdatedPage = confluenceService.findPageByTitle(initialPage.getTitle());
        assertNotNull(savedUpdatedPage);
        assertTrue(savedUpdatedPage.getContent().contains(updatedPage.getContent()));
    }

    @Test
    public void testCreatePageWithAttachment() {
        final ConfluencePage page = new ConfluencePage();
        page.setTitle("Test Page With Attachment - " + UUID.randomUUID());
        page.setContent("Hello, Confluence!");
        final Long pageId = confluenceService.createPage(page);
        final Path attachmentPath = Paths.get("pics/picture.jpg");
        confluenceService.createAttachment(pageId, attachmentPath.toString());

        final String attachmentId =
            confluenceService.getAttachmentId(pageId, "picture.jpg");
        assertNotNull(attachmentId);
    }

    @Test(expected = ConfluenceException.class)
    public void testCreatePageWithUnknownMacro() {
        ConfluencePage page = new ConfluencePage();
        page.setTitle("Test Page With Invalid Macro - " + UUID.randomUUID());
        page.setContent("Hello, {unknown_macro}!");
        confluenceService.createPage(page);
    }

    @Test
    public void testAddLabel() {
        final ConfluencePage page = new ConfluencePage();
        page.setTitle("Test Page With Labels - " + UUID.randomUUID());
        page.setContent("This is a very simple Confluence page with a labels.");

        final Long pageId = confluenceService.createPage(page);

        confluenceService.addLabels(pageId, Arrays.asList("label1", "label2"));

        final ConfluencePage savedPage =
            confluenceService.findPageByTitle(page.getTitle());
        assertNotNull(savedPage);
        assertEquals(2, savedPage.getLabels().size());
        assertTrue(savedPage.getLabels().contains("label1"));
        assertTrue(savedPage.getLabels().contains("label2"));
    }


}