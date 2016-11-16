package com.github.qwazer.markdown.confluence.core.service.impl;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import com.github.qwazer.markdown.confluence.core.service.ConfluenceService;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class ConfluenceServiceImplTest {

    @Autowired
    private ConfluenceService confluenceService;
    ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();

    @Before
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getConfluenceRestApiUrl();
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 200));
    }

    @Test
    public void processAll() throws Exception {
        confluenceService.processAll(confluenceConfig);

        //todo assertions
    }

}