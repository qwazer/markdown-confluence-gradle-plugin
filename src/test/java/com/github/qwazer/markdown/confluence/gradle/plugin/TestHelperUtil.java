package com.github.qwazer.markdown.confluence.gradle.plugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by ar on 11/16/16.
 */
public class TestHelperUtil {

    public static String readCurrentVersion() throws IOException {
        Properties prop = new Properties();
        try (InputStream input = Files.newInputStream(Paths.get("gradle.properties"))) {
            prop.load(input);
            return prop.getProperty("VERSION");
        }
    }


    public static void writeFile(File destination, String content) throws IOException {
        try (BufferedWriter output = new BufferedWriter(new FileWriter(destination))) {
            output.write(content);
        }
    }
}
