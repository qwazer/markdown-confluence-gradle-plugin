package com.github.qwazer.markdown.confluence.core.service;

import org.bsc.markdown.MarkdownParserContext;
import org.bsc.markdown.pegdown.PegdownMarkdownProcessorImpl;

import java.io.IOException;
import java.io.UncheckedIOException;

public class MarkdownServicePegdown extends MarkdownService {

    public MarkdownServicePegdown() {
        super();
    }

    public MarkdownServicePegdown(MarkdownParserContext parserContext) {
        super(parserContext);
    }

    @Override
    String doParseMarkdown(String preprocessedMarkdown) {
        try {
            return new PegdownMarkdownProcessorImpl().processMarkdown(parserContext, preprocessedMarkdown);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
