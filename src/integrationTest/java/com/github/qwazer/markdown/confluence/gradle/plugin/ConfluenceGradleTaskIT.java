package com.github.qwazer.markdown.confluence.gradle.plugin;

import com.github.qwazer.markdown.confluence.Version;
import com.github.qwazer.markdown.confluence.core.AbstractIT;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Anton Reshetnikov on 16 Nov 2016.
 */
public class ConfluenceGradleTaskIT extends AbstractIT {

    private static final Logger LOG = LoggerFactory.getLogger(ConfluenceGradleTaskIT.class);

    // directory where we copy test build scripts and other resources from
    private static final Path GRADLE_BUILDS_PATH =
        Paths.get("src", "integrationTest", "resources", "gradle_builds");

    // a directory that holds build scripts and other test resources required for testing the confluence task
    // defined by this plugin
    @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private Path testProjectRootPath;

    @BeforeClass
    public static void beforeClass() throws Exception {

        // this is to make sure that we have the latest version of the plugin in maven local repo before the tests run
        final String gradlew;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            gradlew = new File("gradlew.bat").getAbsolutePath();
        } else {
            gradlew = new File("gradlew").getAbsolutePath();
        }
        final ProcessBuilder builder = new ProcessBuilder(gradlew, "publishToMavenLocal");
        LOG.info("Running: {}", builder.command());
        final Process process = builder.start();
        final int exitCode = process.waitFor();
        // Process the exit code
        LOG.info("Process exited with code: {}", exitCode);
    }

    @Before
    public void setup() throws IOException {

        testProjectRootPath = testProjectDir.getRoot().toPath();

        // copying the settings.gradle file from the test resources folder (src/integrationTest/resources/gradle_builds)
        // to the testProjectDir folder
        // before copying, the '$VERSION' placeholder is substituted with the current plugin's development version
        final Path srcSettingsFilePath = GRADLE_BUILDS_PATH.resolve("settings.gradle");
        final String settingsFileContent = new String(Files.readAllBytes(srcSettingsFilePath))
            .replaceAll("\\$VERSION", Version.PROJECT_VERSION);
        final Path dstSettingsFilePath =
            testProjectRootPath.resolve("settings.gradle");
        Files.write(dstSettingsFilePath, settingsFileContent.getBytes(StandardCharsets.UTF_8));

        final String projectVersionProperty = "version=" + Version.PROJECT_VERSION;
        Files.write(
            testProjectRootPath.resolve("gradle.properties"),
            projectVersionProperty.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * This test verifies that confluence task configuration defined in
     * src/integrationTest/resources/gradle_builds/build_1.gradle is correctly applied to Confluence and that
     * the page tree is created according to the defined configuration.
     * As of now, this task only verifies that the confluence Gradle task completes successfully.
     */
    @Test
    public void testConfluenceTask() throws IOException {

        final String projectVersion = Version.PROJECT_VERSION;

        // Because the project's README.md file defines the link to the pics/picture.jpg file, we copy that
        // file to the testProjectDir, so that it's available when the confluence Gradle task runs in the
        // testProjectDir
        final Path picsFolderPath = testProjectDir.newFolder("pics").toPath();
        Files.copy(Paths.get("pics", "picture.jpg"), picsFolderPath.resolve("picture.jpg"));

        // src/integrationTest/resources/gradle_builds/build_1.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("build_1.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // project's main README.md is copied to $testProjectDir
        final Path srcReadmeFilePath = Paths.get("README.md");
        final Path dstReadmeFilePath = testProjectRootPath.resolve("README.md");
        Files.copy(srcReadmeFilePath, dstReadmeFilePath);

        // RELEASES.md file is created on the fly and added to $testProjectDir
        final Path releasesFilePath = testProjectDir.newFile("RELEASES.md").toPath();
        final String releasesFileContent =
            "{children:reverse=true|sort=creation|style=h4|excerpt=none|first=99|depth=2|all=true}";
        Files.write(releasesFilePath, releasesFileContent.getBytes(StandardCharsets.UTF_8));

        // RELEASE-$projectVersion.md file is created on the fly and added to $testProjectDir
        final Path releaseFilePath = testProjectDir.newFile("RELEASE-" + projectVersion + ".md").toPath();
        final String releaseFileContent =
            "h1. " + projectVersion + "\n\nChanges in this version include...";
        Files.write(releaseFilePath, releaseFileContent.getBytes(StandardCharsets.UTF_8));

        final BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence", "--stacktrace")
            .forwardOutput()
            .build();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);

        final ConfluencePage projectPage = confluenceService.findPageByTitle("markdown-confluence-gradle-plugin");
        assertNotNull(projectPage);

        final ConfluencePage releaseVersionPage = confluenceService.findPageByTitle(projectVersion);
        assertNotNull(releaseVersionPage);
    }

    @Test
    public void testConfluenceTaskWithChildFiles() throws IOException {

        // Because the project's README.md file defines the link to the pics/picture.jpg file, we copy that
        // file to the testProjectDir, so that it's available when the confluence Gradle task runs in the
        // testProjectDir
        final Path picsFolderPath = testProjectDir.newFolder("pics").toPath();
        Files.copy(Paths.get("pics", "picture.jpg"), picsFolderPath.resolve("picture.jpg"));

        // src/integrationTest/resources/gradle_builds/build_2.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("build_2.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // project's main README.md is copied to $testProjectDir
        final Path srcReadmeFilePath = Paths.get("README.md");
        final Path dstReadmeFilePath = testProjectRootPath.resolve("README.md");
        Files.copy(srcReadmeFilePath, dstReadmeFilePath);

        final Path targetSrcPath = testProjectDir.newFolder("src").toPath();
        Files.copy(GRADLE_BUILDS_PATH.resolve("src/DEVELOPERS.md"), targetSrcPath.resolve("DEVELOPERS.md"));
        Files.copy(GRADLE_BUILDS_PATH.resolve("src/SUPPORT.md"), targetSrcPath.resolve("SUPPORT.md"));

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("confluence", "--info", "--stacktrace")
                .withDebug(true)
                .build();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);

        final ConfluencePage developersPage = confluenceService.findPageByTitle("DEVELOPERS");
        assertNotNull(developersPage);

        final ConfluencePage supportPage = confluenceService.findPageByTitle("SUPPORT");
        assertNotNull(supportPage);
    }

    @Test
    public void testEscapeUnknownMacro() throws IOException {
        // Unknown confluence macros are escaped as text, so it doesn't fail having when having macro-like content.

        // src/integrationTest/resources/gradle_builds/build_4.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("build_4.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file with invalid Markdown markup
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        Files.write(readmeFilePath, "Markdown file with ${strange_macro}.".getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .build();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);

        final ConfluencePage projectPage = confluenceService.findPageByTitle("markdown-confluence-gradle-plugin");
        assertNotNull(projectPage);
        assertTrue(projectPage.getContent().contains("${strange_macro}"));

    }

    @Test
    public void testInvalidConfigurationPageTitleTheSameAsParentTitle() throws IOException {

        // src/integrationTest/resources/gradle_builds/invalid_config_1.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("invalid_config_1.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file with invalid Markdown markup
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        Files.write(readmeFilePath, "# Whatever".getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .buildAndFail();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("Page title cannot be the same as page parent title"));
    }

    @Test
    public void testInvalidConfigurationMissingAuthentication() throws IOException {

        // src/integrationTest/resources/gradle_builds/invalid_config_2.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("invalid_config_2.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file with invalid Markdown markup
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        Files.write(readmeFilePath, "# Whatever".getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .buildAndFail();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("Cannot query the value of extension 'confluence' property 'authentication' because it has no value available"));
    }

    @Test
    public void testInvalidConfigurationInvalidRestApiUrl() throws IOException {

        // src/integrationTest/resources/gradle_builds/invalid_config_3.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("invalid_config_3.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file with invalid Markdown markup
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        Files.write(readmeFilePath, "# Whatever".getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .buildAndFail();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("Invalid restApiUrl value supplied"));
    }

    @Test
    public void testInvalidConfigurationNoRestApiUrl() throws IOException {

        // src/integrationTest/resources/gradle_builds/invalid_config_4.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("invalid_config_4.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file with invalid Markdown markup
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        Files.write(readmeFilePath, "# Whatever".getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .buildAndFail();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("Cannot query the value of extension 'confluence' property 'restApiUrl' because it has no value available"));
    }

    @Test
    public void testInvalidConfigurationBlankSpaceKey() throws IOException {

        // src/integrationTest/resources/gradle_builds/invalid_config_5.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("invalid_config_5.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file with invalid Markdown markup
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        Files.write(readmeFilePath, "# Whatever".getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .buildAndFail();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("Confluence space key cannot be blank/empty"));
    }

    @Test
    public void testInvalidConfigurationMarkdownFileNotFound() throws IOException {

        // src/integrationTest/resources/gradle_builds/invalid_config_6.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("invalid_config_6.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence", "--stacktrace")
            .forwardOutput()
            .buildAndFail();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), FAILED);
        assertTrue(result.getOutput().contains("File not found:"));
        assertTrue(result.getOutput().contains("NoSuchFile.md"));
    }

    @Test
    public void testValidConfigurationPageVariablesShouldBeSubstituted() throws IOException {

        final String expectedPageTitle = "Page with variables";

        // src/integrationTest/resources/gradle_builds/build_3.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("build_3.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        // Creating README.md file that uses page variables
        final Path readmeFilePath = testProjectDir.newFile("README.md").toPath();
        final String markdown =
            "# Header 1\nThe project name is ${project.name} and project version is: ${project.version}";
        Files.write(readmeFilePath, markdown.getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("confluence")
            .forwardOutput()
            .build();

        final BuildTask buildTask = result.task(":confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);

        final ConfluencePage confluencePage =
            confluenceService.findPageByTitle(expectedPageTitle);
        assertNotNull(confluencePage);
        final String content = confluencePage.getContent();
        assertNotNull(content);
        assertTrue(content.contains("The project name is markdown-confluence-gradle-plugin and project version is: " + Version.PROJECT_VERSION));
    }

    @Test
    public void testMultiModuleProject() throws IOException {

        Files.write(
            testProjectRootPath.resolve("settings.gradle"),
            "\ninclude 'server'\n".getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.APPEND
        );

        // src/integrationTest/resources/gradle_builds/build_3.gradle becomes $testProjectDir/build.gradle
        final Path srcBuildFilePath = GRADLE_BUILDS_PATH.resolve("build_5a.gradle");
        final Path dstBuildFilePath = testProjectRootPath.resolve("build.gradle");
        Files.copy(srcBuildFilePath, dstBuildFilePath);

        final Path submodulePath = testProjectDir.newFolder("server").toPath();
        Files.copy(
            GRADLE_BUILDS_PATH.resolve("build_5b.gradle"),
            submodulePath.resolve("build.gradle")
        );

        // Creating README.md file with invalid Markdown markup
        final String markdown =
            "# ${project.name}\nThe project name is ${project.name} and project version is: ${project.version}. Project's parent name is: ${root.project.name}.";
        Files.write(submodulePath.resolve("README.md"), markdown.getBytes(StandardCharsets.UTF_8));

        BuildResult result = GradleRunner.create()
            .withProjectDir(submodulePath.toFile())
            .withArguments("confluence")
            .forwardOutput()
            .build();

        final BuildTask buildTask = result.task(":server:confluence");
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);

        final String expectedPageTitle = "markdown-confluence-gradle-plugin - server";
        final ConfluencePage confluencePage =
            confluenceService.findPageByTitle(expectedPageTitle);
        assertNotNull(confluencePage);
        final String content = confluencePage.getContent();
        assertNotNull(content);
        assertTrue(content.contains("The project name is server and project version is: " + Version.PROJECT_VERSION + ". Project's parent name is: markdown-confluence-gradle-plugin."));
    }

}