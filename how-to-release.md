# How to release markdown-confluence-gradle-plugin

1. update [gradle.properties](gradle.properties)
2. git commit
3. git tag
4. git push
5. verify if the [jitpack](https://jitpack.io/#qwazer/markdown-confluence-gradle-plugin) release build matching the latest release tag completed successfully
6. publish to https://plugins.gradle.org via `gradle publishPlugins` task (`gradle login` may be required from fresh workstation)
7. update & run test `prev_release_availability.gradle`
8. update [README](README.md)
