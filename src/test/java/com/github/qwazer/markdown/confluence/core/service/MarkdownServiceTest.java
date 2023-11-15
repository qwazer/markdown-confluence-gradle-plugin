package com.github.qwazer.markdown.confluence.core.service;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class MarkdownServiceTest {

    private final MarkdownService markdownService = new MarkdownService();

    @Test
    public void testReplaceProperties() {

        final String content = "# ${project.name}\n" +
            "Gradle plugin to publish markdown pages to confluence ```java\ncode;\n``` _italic_";
        final String pageVarName = "project.name";
        final String pageVarValue = "HELLO_PROJECT";
        final Map<String, String> pageVariables = new HashMap<String, String>() {{
            put(pageVarName, pageVarValue);
        }};
        final String wikiText = markdownService.convertMarkdown2Wiki(content, pageVariables);
        System.out.println(wikiText);
        System.out.println("${" + pageVarName + "}");
        assertFalse(wikiText.contains("${" + pageVarName + "}"));
        assertTrue(wikiText.contains(pageVarValue));
    }

    @Test
    public void testNotReplacePropertiesInCode() {

        final String content = "# ${project.name}\n" +
                "Gradle plugin to publish markdown pages to confluence ```java\n${java.code};\n``` _italic_";
        final String pageVarName = "java.code";
        final String pageVarValue = "String s = new String()";
        final Map<String, String> pageVariables = new HashMap<String, String>() {{
            put(pageVarName, pageVarValue);
        }};
        final String wikiText = markdownService.convertMarkdown2Wiki(content, pageVariables);

        assertFalse(wikiText.contains("${" + pageVarName + "}"));
        assertTrue(wikiText.contains(pageVarValue));
    }

    @Test
    public void testMarkdownLinkReplace() {

        final String content = "[скачать](${url})";
        final String pageVarName = "url";
        final String pageVarValue = "https://localhost";
        final Map<String, String> pageVariables = new HashMap<String, String>() {{
            put(pageVarName, pageVarValue);
        }};
        final String wikiText = markdownService.convertMarkdown2Wiki(content, pageVariables);

        assertFalse(wikiText.contains("${" + pageVarName + "}"));
        assertTrue(wikiText.contains(pageVarValue));
    }
}