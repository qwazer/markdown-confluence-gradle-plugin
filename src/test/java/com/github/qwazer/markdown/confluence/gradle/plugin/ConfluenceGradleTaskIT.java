package com.github.qwazer.markdown.confluence.gradle.plugin;

import com.github.qwazer.markdown.confluence.core.UrlChecker;
import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.github.qwazer.markdown.confluence.gradle.plugin.TestHelperUtil.readCurrentVerion;
import static com.github.qwazer.markdown.confluence.gradle.plugin.TestHelperUtil.writeFile;

import static org.gradle.internal.impldep.org.hamcrest.MatcherAssert.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Anton Reshetnikov on 16 Nov 2016.
 */
public class ConfluenceGradleTaskIT {

    @TempDir
    public File testProjectDir;
    private File buildFile;
    private File readmeFile;


    @BeforeEach
    public void setup() throws IOException {
        buildFile = new File(testProjectDir, "build.gradle");
        readmeFile = new File(testProjectDir,"README.md");
        new File(testProjectDir,"src");
        readmeFile = new File(testProjectDir,"src/file1.md");
        readmeFile = new File(testProjectDir,"src/file2.txt");
        readmeFile = new File(testProjectDir,"src/file3.txt");
        readmeFile = new File(testProjectDir,"src/file4.md");
    }

    @BeforeEach
    public void pingRestAPIUrl(){
        String url = "http://localhost:8090/rest/api";
        assertTrue(UrlChecker.pingConfluence(url, 500), "Url should be available " + url);
    }

    @Test
    public void testConfluenceTask() throws IOException {

        String content = IOUtils.toString(this.getClass().getResource("/gradle_builds/sample_build.gradle")) ;
        String version = readCurrentVerion();
        content = content.replaceAll("\\$VERSION", version);
        writeFile(buildFile, content);
        writeFile(readmeFile, "#hello \n${project.name}");


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toPath().getRoot().toFile())
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
                .withProjectDir(testProjectDir.toPath().getRoot().toFile())
                .withArguments("confluence", "--info", "--stacktrace")
                .withDebug(true)
                .build();

        assertEquals(result.task(":confluence").getOutcome(), SUCCESS);
    }

    @Test
    @Disabled
    public void testVerboseError() throws IOException {

        String content = IOUtils.toString(this.getClass().getResource("/gradle_builds/sample_build.gradle")) ;
        String version = readCurrentVerion();
        content = content.replaceAll("\\$VERSION", version);
        writeFile(buildFile, content);
        writeFile(readmeFile, "${strange_macro}");


        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toPath().getRoot().toFile())
                .withArguments("confluence")
                .buildAndFail();

        assertEquals(result.task(":confluence").getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("The macro \'strange_macro\' is unknown") );
    }



}