package com.github.qwazer.gradle.markdown.confluence.impl;

import org.junit.Test;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class Markdown2WikiServiceImplTest {


    @Test
    public void testSimple() throws Exception {
        Markdown2WikiServiceImpl markdown2XtmlService = new Markdown2WikiServiceImpl();

        String s = "# gradle-markdown-confluence\n" +
                "Gradle plugin to publish markdown pages to confluence ```` java code;```` _italic_";

        String xhtml = markdown2XtmlService.convertMarkdown2Wiki(s);

        System.out.println("xhtml = " + xhtml);

    }
}