# markdown-confluence-gradle-plugin
Gradle plugin to publish markdown pages to confluence 

## Usage and sample configuration

Add next lines to ``build.gradle`` 

````
buildscript {
    repositories {
     
        //repo with plugin
        mavenLocal()

    }
    dependencies {
        classpath 'com.github.qwazer:markdown-confluence-gradle-plugin:0.1-SNAPSHOT'
    }
}

apply plugin: 'com.github.qwazer.markdown-confluence'


````

Sample config

````
confluence {
    ancestorId 95791817
    authentication 'base64-encoded user:pass'
    confluenceRestApiUrl 'https://confluence.acme.com/rest/api/'
    spaceKey 'SAMPLE'
    title $projectName
    baseDir = ''
    sslTrustAll true
}


````



invoke new task ``confluence``



## Thanks

Inspired by
  * [maven-confluence-plugin](https://github.com/bsorrentino/maven-confluence-plugin)
by bsorrentino.
  * [Swagger Confluence](https://gitlab.slkdev.net/starlightknight/swagger-confluence)




