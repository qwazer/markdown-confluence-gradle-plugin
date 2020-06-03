[![Build Status](https://travis-ci.org/qwazer/markdown-confluence-gradle-plugin.svg?branch=master)](https://travis-ci.org/qwazer/markdown-confluence-gradle-plugin)

# markdown-confluence-gradle-plugin
Gradle plugin to publish markdown pages to confluence. 

## Usage and sample configuration.

Add the following lines to ``build.gradle`` 

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.qwazer:markdown-confluence-gradle-plugin:0.9.2'
    }
}

apply plugin: 'com.github.qwazer.markdown-confluence'
```

Sample config

```groovy
confluence {
    authentication 'base64-encoded user:pass'
    restApiUrl 'https://confluence.acme.com/rest/api/'
    spaceKey 'SAMPLE'
    sslTrustAll true
    pageVariables = ['project.name': project.name]
    parseTimeout 2000L

    pages {
        page {
            parentTitle  'projectName'
            title "projectName-${project.version}"
            srcFile  file('release-notes.md')
            labels = ['release-notes', project.name]
        }
        page {
            parentTitle = 'Home'
            title = 'projectName'
            srcFile = file('README.md')
        }
    }
}
```

It's possible to define multiple pages with groovy closures.
For example, the next code snippet will define all *.md files inside the src directory as confluence pages 
with title baseFileName (stripped from path and extension) and parent page title 'parentTitle'.

```groovy
    pages {
        fileTree('src')
                .include('**/*.md')
                .collect { file ->
            page {
                parentTitle = 'parentTitle'
                title = file.name.take(file.name.lastIndexOf('.'))
                srcFile = file
            }
        }
    }

```

## Inline images.

In versions 0.9.2 and up any image defined in your markdown page will be uploaded to as an 
attachment to the page. So if you have, for example, `![alt text](uri "Title")` as an inline link
to an image and the `uri` is the path to file on your filesystem then that file
will be uploaded to the confluence page as an attachment, and a link to it
will be inserted into the confluence page in the correct place.

### Note for Spring Boot user

Can conflict with Spring Boot 1.x. Please vote for [#9](https://github.com/qwazer/markdown-confluence-gradle-plugin/issues/9) to solve or use version 0.7.

### Description of config parameters


| parameter        | datatype           | optional | description                                                                                                                                                               |
|:-----------------|:-------------------|:---------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| authentication   | String             | no       | 'user:pass'.bytes.encodeBase64().toString()                                                                                                                               |
| restApiUrl       | String             | no       | Confluence REST API URL                                                                                                                                                   |
| spaceKey         | String             | no       | Confluence space key                                                                                                                                                      |
| sslTrustAll      | Boolean            | yes      | Setting to ignore self-signed and unknown sertificate errors. Usefull in some corporate enviroments                                                                       |
| pageVariables    | Map<String,String> | yes      | Map of page variables, for example ```${project.name}``` in source file content will substituted by value of variable                                                     |
| parseTimeout     | Long               | yes      | Timeout parameter for Markdown serializer                                                                                                                                 |
| pages            | Closure            | no       | Collection of Page Closures. If this config contain several pages, these will be ordered according their parent-child relationship                                        |
| page.parentTitle | String             | no       | Parent page title, will use to resovle actual page ancestorId  against Confluence instance                                                                                |
| page.title       | String             | no       | Page title                                                                                                                                                                |
| page.srcFile     | File               | no       | File with source of wiki page in markdown format (can be mixed with [Confluence Wiki Markup](https://confluence.atlassian.com/doc/confluence-wiki-markup-251003035.html)) |
| page.labels      | Collection<String> | no       | Collection of labels of the Confluence page                                                                                                                               |




### Run the build
```bash
gradle confluence
```


## Thanks

Inspired by
  * [maven-confluence-plugin](https://github.com/bsorrentino/maven-confluence-plugin)
by bsorrentino.
  * [Swagger Confluence](https://gitlab.slkdev.net/starlightknight/swagger-confluence)




