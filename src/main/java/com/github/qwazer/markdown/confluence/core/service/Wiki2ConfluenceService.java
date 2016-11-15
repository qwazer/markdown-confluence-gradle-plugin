package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public interface Wiki2ConfluenceService {

    void postWikiToConfluence(ConfluenceConfig confluenceConfig, String wiki);

}
