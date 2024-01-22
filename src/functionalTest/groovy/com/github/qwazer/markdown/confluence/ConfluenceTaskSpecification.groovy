package com.github.qwazer.markdown.confluence

import com.github.qwazer.markdown.confluence.core.OkHttpUtils
import com.github.qwazer.markdown.confluence.core.service.ConfluenceService
import com.github.qwazer.markdown.confluence.gradle.plugin.AuthenticationType
import okhttp3.OkHttpClient
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Paths

class ConfluenceTaskSpecification extends Specification {

    static final def CONFLUENCE_BASE_URL = 'http://localhost:8090/rest/api'
    static final def CONFLUENCE_SPACE_KEY = 'SN'
    static final def CONFLUENCE_AUTHENTICATION_TYPE = AuthenticationType.BASIC
    static final def CONFLUENCE_AUTHENTICATION = 'admin:admin'.bytes.encodeBase64().toString()

    // used to assert builds outcomes
    ConfluenceService confluenceService

    @TempDir
    File testProjectDir
    File buildFile
    File settingsFile
    File markdownFile
    GradleRunner gradleRunner

    def setup() {
        def authorizationHeaderValue =
            CONFLUENCE_AUTHENTICATION_TYPE.getAuthorizationHeader(CONFLUENCE_AUTHENTICATION)
        def httpClient = new OkHttpClient.Builder()
            .addInterceptor(OkHttpUtils.getAuthorizationInterceptor(authorizationHeaderValue))
            .build()
        confluenceService = new ConfluenceService(CONFLUENCE_BASE_URL, CONFLUENCE_SPACE_KEY, httpClient)
        confluenceService.getOrCreateSpace(CONFLUENCE_SPACE_KEY)

        settingsFile = new File(testProjectDir, "settings.gradle")
        settingsFile << """\
        rootProject.name = 'markdown-confluence-gradle-plugin'
        """.stripIndent()

        buildFile = new File(testProjectDir, "build.gradle")
        buildFile << """\
        plugins {
            id 'com.github.qwazer.markdown-confluence'
        }
        
        allprojects {
            pluginManager.withPlugin('com.github.qwazer.markdown-confluence') {
                confluence {
                    authenticationTypeString = '${CONFLUENCE_AUTHENTICATION_TYPE.name()}'
                    authentication = '${CONFLUENCE_AUTHENTICATION}'
                    restApiUrl = '${CONFLUENCE_BASE_URL}'
                    spaceKey = '${CONFLUENCE_SPACE_KEY}'
                    sslTrustAll = true
                }
            }
        }
        
        """.stripIndent()

        markdownFile = new File(testProjectDir, "README.md")

        gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir)
            .withArguments("confluence", "--stacktrace")
            .forwardOutput()
    }

    def "Page variables should be substituted before the page is published to Confluence"() {

        given:
        def pageTitle = "Page With Substituted Variables"

        markdownFile << """\
        # Header 1\nThe project name is \${project.name} and project version is: \${project.version}
        """.stripIndent()

        // this overrides the common build file created in the setup method
        buildFile << """\
        confluence {
            pageVariables = ['project.name': project.name, 'project.version': project.version]
            pages {
                "${pageTitle}" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """

        when:
        def result = gradleRunner
            .withArguments("confluence", "-Pversion=1.0", "--stacktrace")
            .build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS

        and:
        def publishedPage = confluenceService.findPageByTitle(pageTitle)
        assert publishedPage != null
        def publishedPageContent = publishedPage.content
        assert publishedPageContent.contains("The project name is markdown-confluence-gradle-plugin and project version is: 1.0")

    }

    def "Project README file should be published - Commonmark"() {

        given:
        buildFile << """
        confluence {
            parserType = "commonmark"
            pages {
                "\${project.name}-commonmark" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()
        // using project's README.md markdown file as the file to publish to Confluence
        markdownFile << new File('README.md').text

        // project's README file refers to the pics/picture.jpg picture and hence we need to copy it to the project's
        // test directory retaining the directory structure
        Files.createDirectories(testProjectDir.toPath().resolve("pics"))
        Files.copy(Paths.get("pics", "picture.jpg"), testProjectDir.toPath().resolve("pics/picture.jpg"))

        when:
        def result = gradleRunner.build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS

        and:
        def publishedPageTitle = "markdown-confluence-gradle-plugin-commonmark"
        def publishedPage = confluenceService.findPageByTitle(publishedPageTitle)
        assert publishedPage != null

    }

    def "Project README file should be published - Pegdown"() {

        given:
        buildFile << """
        confluence {
            parserType = "pegdown"
            pages {
                "\${project.name}-pegdown" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()
        // using project's README.md markdown file as the file to publish to Confluence
        markdownFile << new File('README.md').text

        // project's README file refers to the pics/picture.jpg picture and hence we need to copy it to the project's
        // test directory retaining the directory structure
        Files.createDirectories(testProjectDir.toPath().resolve("pics"))
        Files.copy(Paths.get("pics", "picture.jpg"), testProjectDir.toPath().resolve("pics/picture.jpg"))

        when:
        def result = gradleRunner.build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS

        and:
        def publishedPageTitle = "markdown-confluence-gradle-plugin-pegdown"
        def publishedPage = confluenceService.findPageByTitle(publishedPageTitle)
        assert publishedPage != null

    }

    def "Image referred in the markdown should be uploaded to the published Confluence page - Commonmark"() {

        given:
        def pageTitle = "Page With An Image (Commonmark)"
        buildFile << """
        confluence {
            pages {
                "${pageTitle}" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()
        // using project's README.md markdown file as the file to publish to Confluence
        markdownFile << "![This is a picture](pics/picture.jpg \"Extra title\")"

        Files.createDirectories(testProjectDir.toPath().resolve("pics"))
        Files.copy(Paths.get("pics", "picture.jpg"), testProjectDir.toPath().resolve("pics/picture.jpg"))

        when:
        def result = gradleRunner.build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS

        and:
        def publishedPage = confluenceService.findPageByTitle(pageTitle)
        assert publishedPage != null
        assert publishedPage.content.contains("ac:image><ri:attachment ri:filename=\"picture.jpg\"")

    }

    def "Image referred in the markdown should be uploaded to the published Confluence page - Pegdown"() {

        given:
        def pageTitle = "Page With An Image (Pegdown)"
        buildFile << """
        confluence {
            pages {
                "${pageTitle}" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()
        // using project's README.md markdown file as the file to publish to Confluence
        markdownFile << "![This is a picture](pics/picture.jpg \"Extra title\")"

        Files.createDirectories(testProjectDir.toPath().resolve("pics"))
        Files.copy(Paths.get("pics", "picture.jpg"), testProjectDir.toPath().resolve("pics/picture.jpg"))

        when:
        def result = gradleRunner.build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS

        and:
        def publishedPage = confluenceService.findPageByTitle(pageTitle)
        assert publishedPage != null
        assert publishedPage.content.contains("ac:image><ri:attachment ri:filename=\"picture.jpg\"")

    }

    def "Page hierarchy should be correctly created in Confluence irrespective of page declaration order"() {

        given:
        buildFile << """
        confluence {
            pages {
                "Child" {
                    parentTitle = "Parent"
                    srcFile = file("CHILD.md")
                }
                "Parent" {
                    parentTitle = "Grandparent"
                    srcFile = file("PARENT.md")
                }
                "Grandparent" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()

        // The main README file is used as a source for the Grandparent page
        markdownFile << """\
        # Grandparent page
        This page is the top level page.
        """.stripIndent()

        def parentPageMarkdown = new File(testProjectDir, "PARENT.md")
        parentPageMarkdown << """\
        # Parent page
        This page is a child of the `Grandparent` page and a parent page of the `Child` page.
        """.stripIndent()

        def childPageMarkdown = new File(testProjectDir, "CHILD.md")
        childPageMarkdown << """\
        # Child page
        This page is a child of the `Parent` page and it should *NOT* have its own child pages.
        """.stripIndent()

        when:
        def result = gradleRunner.build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS

        and:
        def grandparentPage = confluenceService.findPageByTitle("Grandparent")
        assert grandparentPage != null
        def parentPage = confluenceService.findPageByTitle("Parent")
        assert parentPage != null
        assert parentPage.ancestorId == grandparentPage.id
        def childPage = confluenceService.findPageByTitle("Child")
        assert childPage != null
        assert childPage.ancestorId == parentPage.id

    }

    /**
     * Test project dir structure:
     *
     * multi-module-project/
     *   subproject1/
     *     build.gradle
     *     README.md
     *   subproject2/
     *     build.gradle
     *     README.md
     *   build.gradle
     *   README.md
     *   settings.gradle
     *
     * The root project and both sub-projects define markdown files (all named README.md) to be published during
     * the :confluence task execution.
     *
     * The pages that are published by sub-projects are children of the page published by the root project.
     *
     * The page published by the root project uses a Confluence macro to list its child pages. The macro is correctly
     * parsed only by the Pegdown parser.
     */
    def "All pages configured in a multi-module Gradle project should be published"() {

        given:
        def rootProjectName = 'multi-module-project'
        // this overrides the common settings.gradle file created in the setup method
        settingsFile.withWriter { writer ->
            writer.write("""\
            rootProject.name = '${rootProjectName}'
            
            include 'subproject1'
            include 'subproject2'
            """.stripIndent()
            )
        }

        def subproject1 = Files.createDirectories(testProjectDir.toPath().resolve("subproject1"))
        def subproject1BuildFile = new File(subproject1.toFile(), "build.gradle")
        subproject1BuildFile << """\
        plugins {
            id 'com.github.qwazer.markdown-confluence'
        }
        
        confluence {
            pageVariables = ['project.name': project.name, 'rootProject.name': project.rootProject.name]
            pages {
                "\${project.rootProject.name} - \${project.name}" {
                    parentTitle = "${rootProjectName}"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()

        def subproject1MarkdownFile = new File(subproject1.toFile(), "README.md")
        subproject1MarkdownFile << """\
        # \${project.name}
        
        This is a page documenting the the subproject named \${project.name}.
        
        This subproject is a child of a \${rootProject.name} project.
        """.stripIndent()

        def subproject2 = Files.createDirectories(testProjectDir.toPath().resolve("subproject2"))
        def subproject2BuildFile = new File(subproject2.toFile(), "build.gradle")
        subproject2BuildFile << """\
        plugins {
            id 'com.github.qwazer.markdown-confluence'
        }
        
        confluence {
            pageVariables = ['project.name': project.name, 'rootProject.name': project.rootProject.name]
            pages {
                "\${project.rootProject.name} - \${project.name}" {
                    parentTitle = "${rootProjectName}"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()

        def subproject2MarkdownFile = new File(subproject2.toFile(), "README.md")
        subproject2MarkdownFile << """\
        # \${project.name}
        
        This is a page documenting the the subproject named \${project.name}.
        
        This subproject is a child of a \${rootProject.name} project.
        """.stripIndent()

        // root project's page to be published - this page is a parent page of the pages configured in subprojects
        buildFile << """\
        confluence {
            parserType = 'pegdown'
            pageVariables = ['project.name': project.name]
            pages {
                "\${project.name}" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """.stripIndent()

        markdownFile << """\
        # \${project.name}
        
        This is a page documenting a root project of a multi-module Gradle project named \${project.name}.
        
        Child pages:
        
        {children:depth=1}
        """.stripIndent()

        when:
        def result = gradleRunner.build()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.SUCCESS
        def rootPage = confluenceService.findPageByTitle(rootProjectName)
        assert rootPage != null
        def rootPageContent = rootPage.content
        // this assertion fails when using the Commonmark parser, hence the Pegdown parser is specified
        // in the configuration
        assert rootPageContent.contains("ac:structured-macro ac:name=\"children\"")

        and:
        def subproject1Page = confluenceService.findPageByTitle("multi-module-project - subproject1")
        assert subproject1Page != null
        assert subproject1Page.ancestorId == rootPage.id
        def subproject1PageContent = subproject1Page.content
        assert subproject1PageContent.contains("This is a page documenting the the subproject named subproject1.")

        and:
        def subproject2Page = confluenceService.findPageByTitle("multi-module-project - subproject2")
        assert subproject2Page != null
        assert subproject2Page.ancestorId == rootPage.id
        def subproject2PageContent = subproject2Page.content
        assert subproject2PageContent.contains("This is a page documenting the the subproject named subproject2.")

    }

    def "Confluence task should fail when page title is the same as parent page title"() {

        given:
        markdownFile << """\
        # README
        """.stripIndent()

        buildFile << """\
        confluence {
            pages {
                "Releases" {
                    parentTitle = "Releases"
                    srcFile = file("README.md")
                    labels = ["\${project.name}"]
                }
            }
        }
        """.stripIndent()

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("Page title cannot be the same as page parent title")

    }

    def "Confluence task should fail when authentication is not set"() {

        given:
        markdownFile << """\
        # README
        """.stripIndent()

        // this overrides the common build file created in the setup method
        buildFile.withWriter { writer ->
            writer.write("""\
            plugins {
                id 'com.github.qwazer.markdown-confluence'
            }
            
            confluence {
                authenticationTypeString = '${CONFLUENCE_AUTHENTICATION_TYPE.name()}'
                restApiUrl = '${CONFLUENCE_BASE_URL}'
                spaceKey = '${CONFLUENCE_SPACE_KEY}'
            }
            """.stripIndent()
            )
        }

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("Cannot query the value of extension 'confluence' property 'authentication' because it has no value available")

    }

    def "Confluence task should fail when restApiUrl is invalid"() {

        given:
        markdownFile << """\
        # README
        """.stripIndent()

        buildFile << """\
        confluence {
            restApiUrl = 'Not a valid URL'
        }
        """.stripIndent()

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("Invalid restApiUrl value supplied")

    }

    def "Confluence task should fail when restApiUrl is not set"() {

        given:
        markdownFile << """\
        # README
        """.stripIndent()

        // this overrides the common build file created in the setup method
        buildFile.withWriter { writer ->
            writer.write("""\
            plugins {
                id 'com.github.qwazer.markdown-confluence'
            }
            
            confluence {
                authenticationTypeString = '${CONFLUENCE_AUTHENTICATION_TYPE.name()}'
                authentication = '${CONFLUENCE_AUTHENTICATION}'
                spaceKey = '${CONFLUENCE_SPACE_KEY}'
            }
            """.stripIndent()
            )
        }

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("Cannot query the value of extension 'confluence' property 'restApiUrl' because it has no value available")

    }

    def "Confluence task should fail when blank spaceKey is set"() {

        given:
        markdownFile << """\
        # README
        """.stripIndent()

        buildFile << """\
        confluence {
            spaceKey = ' '
        }
        """.stripIndent()

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("Confluence space key cannot be blank/empty")

    }

    def "Confluence task should fail when markdown file to be published does not exist"() {

        given:
        buildFile << """\
        confluence {
            pages {
                "Releases" {
                    parentTitle = "Releases"
                    srcFile = file("NoSuchFile.md")
                    labels = ["\${project.name}"]
                }
            }
        }
        """.stripIndent()

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("File not found:")
        assert result.output.contains("NoSuchFile.md")

    }

    def "Confluence task should fail when authenticationTypeString is invalid"() {

        given:
        markdownFile << """\
        # README
        """.stripIndent()

        buildFile << """\
        confluence {
            authenticationTypeString = 'wrong'
        }
        """.stripIndent()

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        assert result.output.contains("No enum constant")

    }

    def "Confluence task should fail when parserType value is invalid"() {

        given:
        def parserType = "fancyParser"
        markdownFile << """\
        # README
        """.stripIndent()

        buildFile << """\
        confluence {
            parserType = '${parserType}'
        }
        """.stripIndent()

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED
        def expectedOutput = "Unknown parser type specified: " + parserType.toLowerCase()
        assert result.output.contains(expectedOutput)

    }

    def "Confluence task should fail when using unknown Confluence macro with the Pegdown parser"() {

        given:
        def pageTitle = "Page With Unknown Macro"

        markdownFile << """\
        # Page with unknown macro
        
        Markdown file with {unknown_macro}.
        """.stripIndent()

        // this overrides the common build file created in the setup method
        buildFile << """\
        confluence {
            parserType = 'pegdown'
            pages {
                "${pageTitle}" {
                    parentTitle = "Home"
                    srcFile = file("README.md")
                }
            }
        }
        """

        when:
        def result = gradleRunner.buildAndFail()

        then:
        assert result.task(":confluence").outcome == TaskOutcome.FAILED

    }

}
