package com.github.qwazer.markdown.confluence.core.service.markdown;

import org.bsc.markdown.commonmark.CommonmarkConfluenceWikiVisitor;
import org.bsc.markdown.MarkdownParserContext;
import org.bsc.markdown.commonmark.extension.NoticeBlockExtension;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.*;
import org.commonmark.node.*;

import java.util.*;

// Copied from org.bsc.markdown

/**
 * @author bsorrentino
 */
public abstract class WikiConfluenceSerializer extends CommonmarkConfluenceWikiVisitor {


    public WikiConfluenceSerializer(MarkdownParserContext parseContext) {
        super(parseContext);
    }

    protected static String replaceProperties(String input, Map<String, String> replaceMap) {
        if (input == null || replaceMap == null || replaceMap.isEmpty()) {
            return input;
        }
        for (String key : replaceMap.keySet()) {
            input = input.replace("${" + key + "}", replaceMap.get(key));
        }
        return input;
    }

    public static WikiParser wikiParser() {
        return new WikiParser();
    }

    public static class WikiParser {

        private final List<Extension> extensions = Arrays.asList(StrikethroughExtension.create(), TablesExtension.create(), NoticeBlockExtension.create());

        private final org.commonmark.parser.Parser parser = org.commonmark.parser.Parser.builder().extensions(extensions).build();

        public final Node parse(String content) {
            return parser.parse(content);
        }

        public final String parseMarkdown(MarkdownParserContext context, String content, Map<String, String> pageVariables) {
            final String replacedContent = replaceProperties(content, pageVariables);
            final Node node = parser.parse(replacedContent);

            final CommonmarkConfluenceWikiVisitor visitor = new CommonmarkConfluenceWikiVisitor(context);

            node.accept(visitor);

            return visitor.toString();
        }
    }
}