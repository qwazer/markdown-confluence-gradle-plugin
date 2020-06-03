# How to release markdown-confluence-gradle-plugin

1. update [gradle.properties](gradle.properties)
2. git commit
3. git tag
4. git push
5. wait for upload in bintray from CI build
6. go to bintray https://bintray.com/qwazer/maven and publish manually from authorized account
7. publish to https://plugins.gradle.org via `gradle publishPlugins` task (`gradle login` may required from fresh workstation)
8. update & run test `prev_release_availability.gradle`
9. update [README](README.md)
