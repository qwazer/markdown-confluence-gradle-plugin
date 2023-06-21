# How to release markdown-confluence-gradle-plugin

1. update [gradle.properties](gradle.properties)
2. git commit
3. git tag
4. git push
6. go to jitpack, run the build
7. publish to https://plugins.gradle.org via `gradle publishPlugins` task (`gradle login` may required from fresh workstation)
8. update & run test `prev_release_availability.gradle`
9. update [README](README.md)
