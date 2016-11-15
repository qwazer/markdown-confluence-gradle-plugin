package com.github.qwazer.gradle.markdown.confluence.impl;

import com.github.qwazer.gradle.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.gradle.markdown.confluence.service.FileReaderService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public String readFile(ConfluenceConfig confluenceConfig) throws IOException {
        String baseDir =  confluenceConfig.getBaseDir();
        String baseFile =  confluenceConfig.getBaseFile();

        String content=null;

        File file = new File(baseDir, baseFile);
        FileReader fileReader = new FileReader(file);
        char[] chars = new char[(int) file.length()];
        fileReader.read(chars);
        content = new String(chars);
        fileReader.close();

        return content;

    }
}
