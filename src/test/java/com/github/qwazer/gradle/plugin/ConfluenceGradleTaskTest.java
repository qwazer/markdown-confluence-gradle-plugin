package com.github.qwazer.gradle.plugin;

import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.gradle.testkit.runner.TaskOutcome.*;

/**
 * Created by Anton Reshetnikov on 16 Nov 2016.
 */
public class ConfluenceGradleTaskTest {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;
    private File readmeFile;


    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        readmeFile = testProjectDir.newFile("README.md");
    }

    @Test
    public void testConfluenceTask() throws IOException {


        String content = IOUtils.toString(this.getClass().getResource("/build.gradle.sample")) ;
        String version = readCurrentVerion();
        content = content.replaceAll("\\$VERSION", version);
        writeFile(buildFile, content);
        writeFile(readmeFile, "==hello");


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("confluence")
                .withDebug(true)
                .build();

        assertTrue(result.getOutput().contains("Hello world!"));
        assertEquals(result.task(":confluence").getOutcome(), SUCCESS);
    }



    private static String readCurrentVerion() throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream("gradle.properties");
        prop.load(input);
        return prop.getProperty("VERSION");

    }


    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }



}