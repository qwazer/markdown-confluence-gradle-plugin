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
        classpath 'com.github.qwazer:markdown-confluence-gradle-plugin:0.3-RC01'
    }
}
```

Sample config

```groovy
confluence {
    authentication 'base64-encoded user:pass'
    confluenceRestApiUrl 'https://confluence.acme.com/rest/api/'
    spaceKey 'SAMPLE'
    sslTrustAll true
    pageVariables = ['project.name': project.name]

    pages {
        page {
            parentPage  'projectName'
            title "projectName-${project.version}"
            baseFile  file('release-notes.md')
        }
        page {
            parentPage = 'Home'
            title = 'projectName'
            baseFile = file('README.md')
        }
    }
}
```

### Description of config parameters


parameter | datatype | optional | description
------------ | ------------- | -------------| -------------
authentication | String | no | 'user:pass'.bytes.encodeBase64().toString()
confluenceRestApiUrl | String | no |  confluence rest api url
spaceKey | String | no |  space key
sslTrustAll | Boolean | yes |  ignore self-signed and unknown sertificate errors. Usefull in some corporate enviroments
pageVariables | Map<String,String> | yes | map of page variables, for example ```${project.name}``` will substituted by value of variable
pages | Closure | no | Collection of Page Closures. If this config contain several pages, these will be ordered according their parent-child relationship
page.parentPage  | String | no | Parent page title, will use to resovle actual page ancestorId  against Confluence instance
page.title  | String | no | page title
page.baseFile  | File | no | page content





### Run the build
```bash
gradle confluence
```


## Thanks

Inspired by
  * [maven-confluence-plugin](https://github.com/bsorrentino/maven-confluence-plugin)
by bsorrentino.
  * [Swagger Confluence](https://gitlab.slkdev.net/starlightknight/swagger-confluence)




