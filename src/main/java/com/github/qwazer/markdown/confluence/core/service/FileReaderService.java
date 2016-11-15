package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;

import java.io.IOException;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public interface FileReaderService {

    String readFile(ConfluenceConfig confluenceConfig) throws IOException;
}
