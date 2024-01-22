package com.github.qwazer.markdown.confluence.core.service;

import org.bsc.markdown.MarkdownParserContext;

import java.util.Collections;
import java.util.Map;

public abstract class MarkdownService {

    public static final MarkdownParserContext DEFAULT_PARSER_CONTEXT = new MarkdownParserContext() {
        @Override
        public boolean isSkipHtml() {
            return false;
        }

        @Override
        public boolean isLinkPrefixEnabled() {
            return false;
        }
    };

    protected final MarkdownParserContext parserContext;

    public MarkdownService() {
        this.parserContext = DEFAULT_PARSER_CONTEXT;
    }

    public MarkdownService(MarkdownParserContext parserContext) {
        this.parserContext = parserContext;
    }

    final String preprocessMarkdown(String markdown, Map<String, String> pageVariables) {
        if (markdown == null || pageVariables == null || pageVariables.isEmpty()) {
            return markdown;
        }
        for (String key : pageVariables.keySet()) {
            markdown = markdown.replace("${" + key + "}", pageVariables.get(key));
        }
        return markdown;
    }

    final String parseMarkdown(String markdown) {
        return parseMarkdown(markdown, Collections.emptyMap());
    }

    final String parseMarkdown(String markdown, Map<String, String> pageVariables) {
        final String preprocessedMarkdown = preprocessMarkdown(markdown, pageVariables);
        return doParseMarkdown(preprocessedMarkdown);
    }

    abstract String doParseMarkdown(String preprocessedMarkdown);

}
