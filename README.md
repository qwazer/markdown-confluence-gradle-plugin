[![Build Status](https://travis-ci.org/qwazer/markdown-confluence-gradle-plugin.svg?branch=master)](https://travis-ci.org/qwazer/markdown-confluence-gradle-plugin)

# markdown-confluence-gradle-plugin
Gradle plugin to publish markdown pages to confluence 

## Usage and sample configuration

Add next lines to ``build.gradle`` 

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.qwazer:markdown-confluence-gradle-plugin:0.8'
    }
}
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

It's possible to define multuply pages with groovy closures.
For example, next code snippet will define all *.md files inside src directory as confluence page 
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


### Note for Spring Boot user

Can conflict with Spring 1.x. Please vote for #9 to solve.

### Description of config parameters


parameter | datatype | optional | description
------------ | ------------- | -------------| -------------
authentication | String | no | 'user:pass'.bytes.encodeBase64().toString()
restApiUrl | String | no |  Confluence REST API URL
spaceKey | String | no |  Confluence space key
sslTrustAll | Boolean | yes |  Setting to ignore self-signed and unknown sertificate errors. Usefull in some corporate enviroments
pageVariables | Map<String,String> | yes | Map of page variables, for example ```${project.name}``` in source file content will substituted by value of variable
parseTimeout | Long | yes | Timeout parameter for Markdown serializer
pages | Closure | no | Collection of Page Closures. If this config contain several pages, these will be ordered according their parent-child relationship
page.parentTitle  | String | no | Parent page title, will use to resovle actual page ancestorId  against Confluence instance
page.title  | String | no | Page title
page.srcFile  | File | no | File with source of wiki page in markdown format (can be mixed with [Confluence Wiki Markup](https://confluence.atlassian.com/doc/confluence-wiki-markup-251003035.html))
page.labels  | Collection<String> | no | Collection of labels of the Confluence page




### Run the build
```bash
gradle confluence
```


## Thanks

Inspired by
  * [maven-confluence-plugin](https://github.com/bsorrentino/maven-confluence-plugin)
by bsorrentino.
  * [Swagger Confluence](https://gitlab.slkdev.net/starlightknight/swagger-confluence)




