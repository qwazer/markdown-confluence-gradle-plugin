package com.github.qwazer.markdown.confluence.gradle.plugin;

import java.io.*;
import java.util.Properties;

/**
 * Created by ar on 11/16/16.
 */
public class TestHelperUtil {

    public static String readCurrentVerion() throws IOException {
        Properties prop = new Properties();
        InputStream input = new FileInputStream("gradle.properties");
        prop.load(input);
        return prop.getProperty("VERSION");

    }


    public static void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
