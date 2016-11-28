package com.github.qwazer.markdown.confluence.gradle.plugin;

import com.github.qwazer.markdown.confluence.core.UrlChecker;
import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;

import static com.github.qwazer.markdown.confluence.gradle.plugin.TestHelperUtil.readCurrentVerion;
import static com.github.qwazer.markdown.confluence.gradle.plugin.TestHelperUtil.writeFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.gradle.testkit.runner.TaskOutcome.*;

/**
 * Created by Anton Reshetnikov on 16 Nov 2016.
 */
public class ConfluenceGradleTaskIT {

    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();
    private File buildFile;
    private File readmeFile;


    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
        readmeFile = testProjectDir.newFile("README.md");
        testProjectDir.newFolder("src");
        readmeFile = testProjectDir.newFile("src/file1.md");
        readmeFile = testProjectDir.newFile("src/file2.txt");
        readmeFile = testProjectDir.newFile("src/file3.txt");
        readmeFile = testProjectDir.newFile("src/file4.md");
    }

    @Before
    public void pingRestAPIUrl(){
        String url = "http://localhost:8090/rest/api";
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 500));
    }

    @Test
    public void testConfluenceTask() throws IOException {

        String content = IOUtils.toString(this.getClass().getResource("/gradle_builds/sample_build.gradle")) ;
        String version = readCurrentVerion();
        content = content.replaceAll("\\$VERSION", version);
        writeFile(buildFile, content);
        writeFile(readmeFile, "#hello \n${project.name}");


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("confluence", "--info", "--stacktrace")
                .withDebug(true)
                .build();

        assertEquals(result.task(":confluence").getOutcome(), SUCCESS);
    }

    @Test
    public void testConfluenceTaskWithChildFiles() throws IOException {

        String content = IOUtils.toString(this.getClass().getResource("/gradle_builds/child_files.gradle")) ;
        String version = readCurrentVerion();
        content = content.replaceAll("\\$VERSION", version);
        writeFile(buildFile, content);
        writeFile(readmeFile, "#hello \n${project.name}");


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("confluence", "--info", "--stacktrace")
                .withDebug(true)
                .build();

        assertEquals(result.task(":confluence").getOutcome(), SUCCESS);
    }

    @Test
    public void testVerboseError() throws IOException {

        String content = IOUtils.toString(this.getClass().getResource("/gradle_builds/sample_build.gradle")) ;
        String version = readCurrentVerion();
        content = content.replaceAll("\\$VERSION", version);
        writeFile(buildFile, content);
        writeFile(readmeFile, "${strange_macro}");


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("confluence")
                .buildAndFail();

        assertEquals(result.task(":confluence").getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("The macro \'strange_macro\' is unknown") );
    }



}