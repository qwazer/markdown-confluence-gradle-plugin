package com.github.qwazer.markdown.confluence;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class Version {

    public static final String PROJECT_VERSION;

    static {
        final Properties gradleProperties = new Properties();
        try(final Reader reader = Files.newBufferedReader(Paths.get("gradle.properties"))) {
            gradleProperties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String version = gradleProperties.getProperty("version");
        if (version == null) {
            throw new RuntimeException("No 'version' property defined in the gradle.properties file");
        }
        PROJECT_VERSION = version;
    }

    private Version() {}

}
