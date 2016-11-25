package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import org.gradle.internal.impldep.com.amazonaws.util.StringMapBuilder;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class MarkdownServiceTest {


    private MarkdownService markdown2XtmlService = new MarkdownService();
    private ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();


    @Test
    public void testReplaceProperties() throws Exception {

        String content = "# ${project.name} \n" +
                "Gradle plugin to publish markdown pages to confluence ```` java code;```` _italic_";
        String projectName = "HELLO_PROJECT";
        String key = "project.name";
        assertTrue(content.contains("${"+key +"}"));
        assertFalse(content.contains(projectName));

        Map<String,String>  stringMap = new StringMapBuilder(key,projectName).build();

        confluenceConfig.setPageVariables(stringMap);

        String wiki = markdown2XtmlService.convertMarkdown2Wiki(content, confluenceConfig);

        assertFalse(wiki.contains("${"+key +"}"));
        assertTrue(wiki.contains(projectName));

    }

    @Test
    public void testNotReplacePropertiesInCode() throws Exception {

        String content = "# ${project.name} \n" +
                "Gradle plugin to publish markdown pages to confluence ```java \n${java.code};\n``` _italic_";
        String key = "java.code";
        String value = "String s = new String()";
        assertTrue(content.contains("${"+key +"}"));
        assertFalse(content.contains(value));

        Map<String,String>  stringMap = new StringMapBuilder(key,value).build();

        confluenceConfig.setPageVariables(stringMap);

        String wiki = markdown2XtmlService.convertMarkdown2Wiki(content, confluenceConfig);

        assertFalse(wiki.contains("${"+key +"}"));
        assertTrue(wiki.contains(value));

    }
}