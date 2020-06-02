# How to release markdown-confluence-gradle-plugin

1. update [gradle.properties](gradle.properties)
2. git commit
3. git tag
4. git push
5. wait for upload in bintray from CI build
6. go to bintray and publish manually
7. publish to gradle.portal
8. update & run test `prev_release_availability.gradle`
9. update [README](README.md)
