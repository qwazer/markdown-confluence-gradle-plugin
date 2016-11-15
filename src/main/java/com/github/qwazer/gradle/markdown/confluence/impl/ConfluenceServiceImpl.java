package com.github.qwazer.gradle.markdown.confluence.impl;

import com.github.qwazer.gradle.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.gradle.markdown.confluence.service.ConfluenceService;
import com.github.qwazer.gradle.markdown.confluence.service.FileReaderService;
import com.github.qwazer.gradle.markdown.confluence.service.Markdown2WikiService;
import com.github.qwazer.gradle.markdown.confluence.service.Wiki2ConfluenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class ConfluenceServiceImpl implements ConfluenceService {

    private final FileReaderService fileReaderService;
    private final Markdown2WikiService markdown2WikiService;
    private final Wiki2ConfluenceService wiki2ConfluenceService;

    @Autowired
    public ConfluenceServiceImpl(FileReaderService fileReaderService,
                                 Markdown2WikiService markdown2WikiService,
                                 Wiki2ConfluenceService wiki2ConfluenceService) {
        this.fileReaderService = fileReaderService;
        this.markdown2WikiService = markdown2WikiService;
        this.wiki2ConfluenceService = wiki2ConfluenceService;
    }

    @Override
    public void processAll(ConfluenceConfig confluenceConfig) throws IOException {
        String plainFileContent = fileReaderService.readFile(confluenceConfig);
        String wikiText = markdown2WikiService.convertMarkdown2Wiki(plainFileContent);
        wiki2ConfluenceService.postWikiToConfluence(confluenceConfig, wikiText);
    }
}
