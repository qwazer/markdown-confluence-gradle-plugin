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
        classpath 'com.github.qwazer:markdown-confluence-gradle-plugin:0.2-RC01'
    }
}
```

Sample config

```groovy
confluence {
    authentication 'base64-encoded user:pass'
    confluenceRestApiUrl 'https://confluence.acme.com/rest/api/'
    spaceKey 'SAMPLE'
    parentPage 'Home'
    title $projectName
    baseFile = file('README.md')
    sslTrustAll true
}
```

### Description of config parameters


parameter | datatype | optional | description
------------ | ------------- | -------------| -------------
authentication | String | no | 'user:pass'.bytes.encodeBase64().toString()
confluenceRestApiUrl | String | no |  confluence rest api url
spaceKey | String | no |  space key
parentPage | String | yes | if not specified will use space home as parent page 
title | File | no | path to file with markdown file
sslTrustAll | Boolean | yes |  ignore self-signed and unknown sertificate errors. Usefull in some corporate enviroments




### Run the build
```bash
gradle confluence
```


## Thanks

Inspired by
  * [maven-confluence-plugin](https://github.com/bsorrentino/maven-confluence-plugin)
by bsorrentino.
  * [Swagger Confluence](https://gitlab.slkdev.net/starlightknight/swagger-confluence)




