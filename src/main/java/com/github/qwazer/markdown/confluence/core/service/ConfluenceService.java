package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;

import java.io.IOException;

/**
 * Created by Anton Reshetnikov on 14 Nov 2016.
 */
public interface ConfluenceService {

    void processAll(ConfluenceConfig confluenceConfig) throws IOException;

}
