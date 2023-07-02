## Release Notes

This document provides an overview of the changes and improvements in each version of the ``com.github.qwazer.markdown-confluence`` plugin.
It is recommended to review the release notes before upgrading to a new version.

### Version 0.10.0

- Dropped the dependency on the Spring Framework.
- OkHttpClient is now used to interact with Confluence REST APIs.
- Jackson is the only JSON processing library.
- The jvm-test-suite plugin has been applied to separate unit & integration tests.
- The Gradle publish-plugin has been updated to the latest version (1.2.0).
- Gradle's integration tests for the confluence task first publish the current SNAPSHOT version of the plugin to mavenLocal, so it's no longer necessary to do it manually before running integration tests.
- Using built-in Java utils for reading and writing files.
- The ``=`` sign is now required when assigning a value to a property. This is because
  the underlying way the plugin's configuration is applied has changed to make the plugin future-proof.
  Part of that change was replacing the previously used ``ConfluenceConfig`` class with the ``ConfluenceExtension`` class.
  The new class also provides the ``pages`` method, but the argument to that method is now
  ``Action<? super NamedDomainObjectContainer<Page>>`` instead of ``Closure``, and the most notable consequence of this
  change is that now the ``pageTitle`` is the name of the page configuration container as shown in the example below.

```groovy
confluence {
    authenticationType = AuthenticationType.BASIC
    authentication = 'username:password'.bytes.encodeBase64().toString() 
    restApiUrl = 'https://confluence.acme.com/rest/api/'
    spaceKey = 'SAMPLE'
    sslTrustAll = true
    pageVariables = ['project.name': project.name]
    parseTimeout = 2000L

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

- 

### Version 0.9.3

- Added support for Confluence Personal Access Token (PAT) authentication.

### Version 0.9.2

- Added support for publishing of inline images referenced in Markdown documents. Since this version, any local image referenced in your markdown file will be uploaded as an
  attachment to the generated Confluence page and correctly linked. Check [README.md](README.md) for details/example.