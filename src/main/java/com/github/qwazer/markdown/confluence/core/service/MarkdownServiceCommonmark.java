package com.github.qwazer.markdown.confluence.core.service;

import org.bsc.markdown.MarkdownParserContext;
import org.bsc.markdown.commonmark.CommonmarkConfluenceWikiVisitor;

public class MarkdownServiceCommonmark extends MarkdownService {

    public MarkdownServiceCommonmark() {
        super();
    }

    public MarkdownServiceCommonmark(MarkdownParserContext parserContext) {
        super(parserContext);
    }

    @Override
    String doParseMarkdown(String preprocessedMarkdown) {
        return CommonmarkConfluenceWikiVisitor.parser().parseMarkdown(parserContext, preprocessedMarkdown);
    }
}
