package com.github.qwazer.gradle.markdown.confluence.service;

import com.github.qwazer.gradle.markdown.confluence.core.ConfluenceConfig;

import java.io.IOException;

/**
 * Created by Anton Reshetnikov on 14 Nov 2016.
 */
public interface ConfluenceService {

    void processAll(ConfluenceConfig confluenceConfig) throws IOException;

}
