package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringConfig.class)
public class MainServiceTest {

    @Autowired
    private MainService mainService;
    ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();

    @BeforeEach
    public void pingRestAPIUrl() {
        String url = TestConfigFactory.testConfluenceConfig().getRestApiUrl();
        assertTrue(UrlChecker.pingConfluence(url, 200), "Url should be available " + url);
    }

    @Test
    public void processAll() throws Exception {
        mainService.processAll(confluenceConfig);

        //todo assertions
    }

}