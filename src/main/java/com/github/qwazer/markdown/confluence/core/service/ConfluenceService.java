package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class ConfluenceService {

    private final FileReaderService fileReaderService;
    private final Markdown2WikiService markdown2WikiService;
    private final WikiToConfluenceService wiki2ConfluenceService;

    @Autowired
    public ConfluenceService(FileReaderService fileReaderService,
                             Markdown2WikiService markdown2WikiService,
                             WikiToConfluenceService wiki2ConfluenceService) {
        this.fileReaderService = fileReaderService;
        this.markdown2WikiService = markdown2WikiService;
        this.wiki2ConfluenceService = wiki2ConfluenceService;
    }

    public void processAll(ConfluenceConfig confluenceConfig) throws IOException {


        for (Page page : confluenceConfig.getPages()) {
            String plainFileContent = fileReaderService.readFile(page);
            String wikiText = markdown2WikiService.convertMarkdown2Wiki(plainFileContent, confluenceConfig);
            wiki2ConfluenceService.postWikiPageToConfluence(page, confluenceConfig, wikiText);
        }
    }



}
