package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.service.markdown.WikiConfluenceSerializer;
import org.bsc.markdown.MarkdownParserContext;

import java.util.Map;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class MarkdownService {

    public MarkdownService() {

    }

    public String convertMarkdown2Wiki(final String markdown, Map<String, String> pageVariables) {

        final MarkdownParserContext context = () -> false;

        return WikiConfluenceSerializer.wikiParser().parseMarkdown(context, markdown, pageVariables);
    }
}
