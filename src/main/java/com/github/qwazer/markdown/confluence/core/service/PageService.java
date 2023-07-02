package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import com.github.qwazer.markdown.confluence.gradle.plugin.ConfluenceExtension;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anton Reshetnikov on 14 Nov 2016.
 */
public class PageService {

    public static final Pattern INLINE_IMAGE_PATTERN = Pattern.compile("!\\[(.*)]\\((\\S+)(.*)\\)");
    private static final Logger LOG = LoggerFactory.getLogger(PageService.class);

    private final ConfluenceService confluenceService;
    private final MarkdownService markdownService;
    private final AttachmentService attachmentService;

    public PageService(final ConfluenceService confluenceService) {
        this(confluenceService, new AttachmentService(confluenceService), new MarkdownService());
    }

    public PageService(
        final ConfluenceService confluenceService,
        final AttachmentService attachmentService,
        final MarkdownService markdownService
    ) {
        Objects.requireNonNull(confluenceService);
        Objects.requireNonNull(attachmentService);
        Objects.requireNonNull(markdownService);

        this.confluenceService = confluenceService;
        this.attachmentService = attachmentService;
        this.markdownService = markdownService;
    }

    public Pair<String, Map<String, Path>> prepareWikiText(ConfluenceExtension.Page page) throws IOException {
        return prepareWikiText(page, Collections.emptyMap());
    }

    // Any images referenced in the markdown need to be uploaded as attachments to the generated Confluence page.
    // An inline image in markdown looks like this: ![alt text](uri "Title")
    // uri can be an external reference (e.g. https://zedplanet.com/images/logo.png) or a local file path
    // e.g. (docs/images/logo.png or /Users/user/home/avatar.png). However, using absolute paths is typically not
    // portable, i.e., there's a little chance that the aforementioned path /Users/user/home/avatar.png will be
    // present on the build server in case you want to automate publishing of wiki pages during your CI builds.
    public Pair<String, Map<String, Path>> prepareWikiText(ConfluenceExtension.Page page, Map<String, String> pageVariables) throws IOException {

        final String markdownText = page.getContent();

        final Map<String, Path> inlineImages = new HashMap<>();
        final Matcher matcher = INLINE_IMAGE_PATTERN.matcher(markdownText);
        while (matcher.find()) {
            // group[0] is the whole matched string
            // group[1] is the alt text
            // group[2] is the uri
            // group[3] is the title - with quotes
            String uri = matcher.group(2);
            // Don't deal with http references or absolute file references
            if (!uri.toLowerCase().startsWith("http") && (!uri.startsWith("/"))) {
                // the path should be relative to the markdown file
                final String parent = page.getSrcFile().getParent();
                final Path path;
                if (parent != null) {
                    path = Paths.get(parent, uri);
                } else {
                    path = Paths.get(uri);
                }
                if (Files.exists(path)) {
                    inlineImages.put(uri, path);
                } else {
                    final String message =
                        String.format("Could not find local image '%s' referenced in the '%s' markdown file", page, page.getSrcFile());
                    throw new ConfluenceException(message);
                }
            }
        }

        // initial version of the wiki text before processing inline images
        String wikiText = markdownService.convertMarkdown2Wiki(markdownText, pageVariables);
        for (Map.Entry<String, Path> entry : inlineImages.entrySet()) {
            final String patternString = "!" + entry.getKey() + "\\|";
            final String replacementString = "!" + entry.getValue().getFileName().toString() + "|";
            wikiText = wikiText.replaceAll(patternString, replacementString);
        }

        return new Pair<>(wikiText, inlineImages);

    }

    public Long publishWikiPage(ConfluenceExtension.Page page) throws IOException {
        return publishWikiPage(page, Collections.emptyMap());
    }

    public Long publishWikiPage(ConfluenceExtension.Page page, Map<String, String> pageVariables) throws IOException {

        final Pair<String, Map<String, Path>> pair = prepareWikiText(page, pageVariables);
        final String wikiText = pair.getFirst();
        final Map<String, Path> images = pair.getSecond();

        ConfluencePage confluencePage =
            confluenceService.findPageByTitle(page.getName());
        if (confluencePage != null) {               // page exists
            LOG.info("Updating existing page: {}", confluencePage);
            confluencePage.setContent(wikiText);
            confluencePage.setLabels(page.getLabels());
            confluenceService.updatePage(confluencePage);
            confluenceService.addLabels(confluencePage.getId(), page.getLabels());
        } else {
            confluencePage = new ConfluencePage();
            confluencePage.setContent(wikiText);
            confluencePage.setTitle(page.getTitle());
            confluencePage.setLabels(page.getLabels());
            final Long ancestorId =
                confluenceService.findAncestorId(page.getParentTitle());
            confluencePage.setAncestorId(ancestorId);
            LOG.info("Creating new Confluence page: {}", confluencePage);
            final Long pageId =
                confluenceService.createPage(confluencePage);
            confluencePage.setId(pageId);
            confluenceService.addLabels(pageId, page.getLabels());
        }

        for (Map.Entry<String, Path> entry : images.entrySet()) {
            attachmentService.postAttachmentToPage(confluencePage.getId(), entry.getValue());
        }

        return confluencePage.getId();
    }

}