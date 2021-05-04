package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.qwazer.markdown.confluence.core.ConfluenceConfig.DEFAULT_PARSE_TIMEOUT;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class MainService {

    private final FileReaderService fileReaderService;
    private MarkdownService markdownService;
    private final PageService pageService;
    private final AttachmentService attachmentService;
    private final ConfluenceService confluenceService;

    @Autowired
    public MainService(FileReaderService fileReaderService,
                       MarkdownService markdownService,
                       PageService pageService,
                       AttachmentService attachmentService,
                       ConfluenceService confluenceService) {
        this.fileReaderService = fileReaderService;
        this.markdownService = markdownService;
        this.pageService = pageService;
        this.attachmentService = attachmentService;
        this.confluenceService = confluenceService;
    }

    public void processAll(ConfluenceConfig confluenceConfig) throws IOException {

        confluenceService.setConfluenceConfig(confluenceConfig);

        if (confluenceConfig.getParseTimeout() != DEFAULT_PARSE_TIMEOUT) {
            this.markdownService = new MarkdownService(confluenceConfig.getParseTimeout());
        }

        List<Page> orderedList = order(confluenceConfig.getPages());
        for (Page page : orderedList) {
            String markdownText = fileReaderService.readFile(page);
            String wikiText = markdownService.convertMarkdown2Wiki(markdownText, confluenceConfig);

            // Holds the original uri and, if its a file uri, the path to the file
            Map<String, Path> imageURIToFilePathMap = new HashMap<>();
            wikiText = processInlineImages(markdownText, wikiText, page.getSrcFile(), imageURIToFilePathMap);

            Long pageId = pageService.postWikiPageToConfluence(page, wikiText);

            for (Map.Entry<String, Path> entry : imageURIToFilePathMap.entrySet()) {
                attachmentService.postAttachmentToPage(pageId, entry.getValue());
            }
        }
    }

    // Any images in the markdown need to be published as attachments to the page.
    // An inline image in markdown looks like this:
    // ![alt text](uri "Title")
    // uri can be an http reference (e.g. https://zedplanet.com/images/logo.png) or a file path
    // e.g. (./docs/images/logo.png or /Users/user/home/avatar.png)
    // Any files need to be uploaded as attachments, the attachment has the filename not the full
    // path so we need to replace the full path in the wikiText
    private String processInlineImages(String markdownText, String wikiText, File srcFile, Map<String, Path> imageURIToFilePathMap) {

        // Regex to match an inline image and extract the alt text, uri and title
        String regex = "!\\[(.*)\\]\\(([^\\s]+)(.*)\\)";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(markdownText);

        String directory = srcFile.getParent();

        while (matcher.find()) {
            // group[0] is the whole matched string
            // group[1] is the alt text
            // group[2] is the uri
            // group[3] is the title - with quotes
            String uri = matcher.group(2);
            // Don't deal with http references or absolute file references
            if (!uri.startsWith("http") && (!uri.startsWith("/"))) {
                Path path;
                if (uri.startsWith("/")) {
                    path = Paths.get(uri);
                } else {
                    path = Paths.get(directory + "/" + uri);
                }
                imageURIToFilePathMap.put(uri, path);
            }
        }

        String adjustedWikiTest = wikiText;
        for (Map.Entry<String, Path> entry : imageURIToFilePathMap.entrySet()) {
            adjustedWikiTest = adjustedWikiTest.replaceAll(entry.getKey(), entry.getValue().getFileName().toString());
        }

        return adjustedWikiTest;
    }

    public static List<Page> order(Collection<Page> pages){
        if (pages.isEmpty()) return Collections.EMPTY_LIST;

        HashMap<Integer,Collection<Page>> group = new HashMap<>();

        Collection<Page> roots = new ArrayList<>();
        Collection<Page> childs = new ArrayList<>();
        for (Page page : pages){
            if (hasParent(page, pages)) {
               childs.add(page);
            } else {
               roots.add(page);
            }
        }

        LinkedList<Page> linkedList = new LinkedList<>();

        linkedList.addAll(roots);
        linkedList.addAll(order(childs));
        return linkedList;
    }

    private static boolean hasParent(Page page, Collection<Page> pages) {
        boolean res = false;
        for (Page curr  : pages){
            if (curr.getTitle().equals(page.getParentTitle())){
                return true;
            }
        }

        return res;
    }
}
