package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.service.Markdown2WikiService;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class Markdown2WikiServiceTest {


    @Test
    public void testSimple() throws Exception {
        Markdown2WikiService  markdown2XtmlService = new Markdown2WikiService();

        String s = "# gradle-markdown-confluence\n" +
                "Gradle plugin to publish markdown pages to confluence ```` java code;```` _italic_";

        String xhtml = markdown2XtmlService.convertMarkdown2Wiki(s);

        Assert.notNull(xhtml);
        //todo more assertions

    }
}