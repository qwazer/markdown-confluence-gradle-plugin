[![Build Status](https://travis-ci.org/qwazer/markdown-confluence-gradle-plugin.svg?branch=master)](https://travis-ci.org/qwazer/markdown-confluence-gradle-plugin)

# markdown-confluence-gradle-plugin

Gradle plugin to publish markdown pages to confluence.

## Usage and sample configuration.

You can define the plugin in ``settings.gradle``

```groovy
pluginManagement {
    repositories {
        maven { url "https://jitpack.io" }
    }

    plugins {
        id "com.github.qwazer.markdown-confluence" version "$VERSION"
    }
}
```

And then apply the plugin in your ``build.gradle`` - if the version is specified in the ``settings.gradle``, you don't
need to repeat it in the ``build.gradle``.

```groovy
plugins {
    id "com.github.qwazer.markdown-confluence"
}
```

Sample config.

```groovy
confluence {
    authenticationType = AuthenticationType.BASIC
    authentication = 'username:password'.bytes.encodeBase64().toString()
    restApiUrl = 'https://confluence.acme.com/rest/api/'
    spaceKey = 'SAMPLE'
    sslTrustAll = true
    pageVariables = ['project.name': project.name]
    parserType = 'commonmark'

    pages {
        "Releases" {
            parentTitle = "Home"
            srcFile = file("RELEASES.md")
            labels = ["release-notes", "${project.version}"]
        }
        "${project.name}" {
            parentTitle = "Releases"
            srcFile = file("README.md")
        }
    }
}
```

It's possible to define multiple pages with groovy closures.
For example, the below code snippet defines all ``*.md`` files inside the ``src`` directory as confluence pages
titled the same as underlying files (minus file path and extension) and having ``parentTitle`` equal to ``Parent Page``.

```groovy
    pages {
        fileTree("src")
            .include("**/*.md")
            .collect { file ->
                def title = file.name.take(file.name.lastIndexOf('.'))
                "${title}" {
                    parentTitle = "Parent Page"
                    srcFile = file
                }
        }
    }
```

## Inline images.

The below Markdown code:

```text
![Picture Alt Text](pics/picture.jpg "Extra title")
```

is translated to the following Confluence wiki:

```text
!picture.jpg|Picture Alt Text!
```

Plus the **pics/picture.jpg** file (relative to the Markdown file) is added as an attachment (named **picture.jpg**)
to the generated Confluence page and correctly linked.


### Description of config parameters

| parameter                | datatype                   | optional | description                                                                                                                                                                                                                                                                              |
|:-------------------------|:---------------------------|:---------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| authenticationType       | Enum                       | yes      | Authentication type to use when calling Confluence APIs, one of: BASIC, PAT (Personal Access Token). Defaults to BASIC when not specified explicitly.                                                                                                                                    |
| authenticationTypeString | String                     | yes      | Overrides the above configuration using a string constant, one of: `BASIC`, `PAT` (Personal Access Token). Defaults to empty string when not specified explicitly.                                                                                                                       |
| authentication           | String                     | no       | 'user:pass'.bytes.encodeBase64().toString() when `authenticationType` is `BASIC`, or token string when `authenticationType` is `PAT`.                                                                                                                                                    |
| restApiUrl               | String                     | no       | Confluence REST API URL.                                                                                                                                                                                                                                                                 |
| spaceKey                 | String                     | no       | Confluence space key.                                                                                                                                                                                                                                                                    |
| sslTrustAll              | Boolean                    | yes      | Setting to ignore self-signed and unknown certificate errors. Useful in some corporate environments.                                                                                                                                                                                     |
| pageVariables            | Map<String, String>        | yes      | Map of page variables. For example, if a Markdown file to be published contains `${myVar}` placeholder, and the `pageVariable` map has an entry with the `myVar` key, then the placeholder in the Markdown file in will be substituted by value of the map entry having the `myVar` key. |
| parserType               | String                     | yes      | Markdown to Confluence wiki parser to use. One of: `commonmark` (default if not specified), `pegdown`.                                                                                                                                                                                   |
| pages                    | Closure                    | no       | Collection of NamedDomainObjectContainer<Page>. If this collection contains multiple pages, they will be ordered according their parent-child relationship.                                                                                                                              |
| page                     | NamedDomainObjectContainer | no       | Name of the container is the title of the page. Check [Declaring DSL configuration container](https://docs.gradle.org/current/userguide/implementing_gradle_plugins.html#declaring_a_dsl_configuration_container).                                                                       |
| page.parentTitle         | String                     | no       | The title of the parent page under which this page should be published. It is used to resolve target page ancestorId in Confluence.                                                                                                                                                      |
| page.srcFile             | File                       | no       | The Markdown file to be published as Confluence wiki page (can be mixed with [Confluence Wiki Markup](https://confluence.atlassian.com/doc/confluence-wiki-markup-251003035.html)).                                                                                                      |
| page.labels              | Collection<String>         | yes      | Collection of labels to be added to the generated Confluence page.                                                                                                                                                                                                                       |  

### Run the build

To trigger the generation of the page(s) configured in your build.gradle(.kts), just run the below command

```bash
./gradlew confluence
```

When the build completes successfully the configured pages will be in Confluence under the specified space.


## Thanks

* Inspired by [maven-confluence-plugin](https://github.com/bsorrentino/maven-confluence-plugin) by bsorrentino.
* [Swagger Confluence](https://gitlab.slkdev.net/starlightknight/swagger-confluence)
