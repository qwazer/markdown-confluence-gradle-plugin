package com.github.qwazer.gradle.markdown.confluence.service;

import com.github.qwazer.gradle.markdown.confluence.core.ConfluenceConfig;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public interface Wiki2ConfluenceService {

    void postWikiToConfluence(ConfluenceConfig confluenceConfig, String wiki);

}
