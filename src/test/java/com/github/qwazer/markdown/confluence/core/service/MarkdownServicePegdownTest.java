package com.github.qwazer.markdown.confluence.core.service;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class MarkdownServicePegdownTest {

    private final MarkdownService markdownService = new MarkdownServicePegdown();

    @Test
    public void testReplaceProperties() {

        final String content = "# ${project.name}\n" +
            "Gradle plugin to publish markdown pages to confluence ```java\ncode;\n``` _italic_";
        final String pageVarName = "project.name";
        final String pageVarValue = "HELLO_PROJECT";
        final Map<String, String> pageVariables = new HashMap<String, String>() {{
            put(pageVarName, pageVarValue);
        }};
        final String wikiText = markdownService.parseMarkdown(content, pageVariables);

        assertFalse(wikiText.contains("${" + pageVarName + "}"));
        assertTrue(wikiText.contains(pageVarValue));
    }

    @Test
    public void testNotReplacePropertiesInCode() {

        final String markdown = "# ${project.name}\n" +
                "Gradle plugin to publish markdown pages to confluence ```java\n${java.code};\n``` _italic_";
        final String pageVarName = "java.code";
        final String pageVarValue = "String s = new String()";
        final Map<String, String> pageVariables = new HashMap<String, String>() {{
            put(pageVarName, pageVarValue);
        }};
        final String wikiText = markdownService.parseMarkdown(markdown, pageVariables);

        assertFalse(wikiText.contains("${" + pageVarName + "}"));
        assertTrue(wikiText.contains(pageVarValue));
    }

    @Test
    public void testMarkdownLinkReplace() {

        final String markdown = "[скачать](${url})";
        final String pageVarName = "url";
        final String pageVarValue = "https://localhost";
        final Map<String, String> pageVariables = new HashMap<String, String>() {{
            put(pageVarName, pageVarValue);
        }};
        final String wikiText = markdownService.parseMarkdown(markdown, pageVariables);

        assertFalse(wikiText.contains("${" + pageVarName + "}"));
        assertTrue(wikiText.contains(pageVarValue));
    }

    @Test
    public void imageLinkShouldNotContainPageTitleVariableAfterMarkdownToConfluenceWikiConversion() {
        final String markdown = "![Image Description](images/image.png)";
        final String wikiText = markdownService.parseMarkdown(markdown);

        assertFalse(wikiText.contains("${page.title}"));
        assertEquals("!image.png|Image Description!", wikiText.trim());
    }

}