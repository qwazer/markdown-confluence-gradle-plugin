package com.github.qwazer.markdown.confluence.gradle.plugin;

import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;

import static com.github.qwazer.markdown.confluence.gradle.plugin.TestHelperUtil.writeFile;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Anton Reshetnikov on 16 Nov 2016.
 */
public class PrevReleaseAvaibilityIT {

    @TempDir
    public File testProjectDir;
    private File buildFile;
    private File readmeFile;


    @BeforeEach
    public void setup() throws IOException {
        buildFile = new File(testProjectDir, "build.gradle");
        readmeFile = new File(testProjectDir, "README.md");
    }


    @Test
    @Disabled
    public void testPrevReleaseAvailability() throws Exception {
        String content = IOUtils.toString(this.getClass().getResource("/gradle_builds/prev_release_availability.gradle")) ;
        writeFile(buildFile, content);
        writeFile(readmeFile, "==hello");
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.toPath().getRoot().toFile())
                .withArguments("confluence", "--info", "--stacktrace")
                .withDebug(true)
                .buildAndFail();
        System.out.println("result.getOutput() = " + result.getOutput());
        assertTrue(result.getOutput().contains("I/O error on GET request for"));
        assertEquals(result.task(":confluence").getOutcome(), FAILED);

    }


}