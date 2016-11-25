package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Service
public class FileReaderService  {
    public String readFile(ConfluenceConfig.Page page) throws IOException {

        File file = page.getSrcFile();
        return new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));

    }
}
