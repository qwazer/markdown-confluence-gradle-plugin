package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.SpringConfig;
import com.github.qwazer.markdown.confluence.core.TestConfigFactory;
import com.github.qwazer.markdown.confluence.core.UrlChecker;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.github.qwazer.markdown.confluence.core.service.ConfluenceService.parseLabels;
import static org.junit.Assert.*;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
public class ConfluenceServiceTest {

    @Autowired
    private ConfluenceService confluenceService;
    private final ConfluenceConfig confluenceConfig = TestConfigFactory.testConfluenceConfig();
    private final ConfluenceConfig.Page page = TestConfigFactory.getPage();

    @Before
    public void pingRestAPIUrl(){
        String url = TestConfigFactory.testConfluenceConfig().getRestApiUrl();
        Assume.assumeTrue( "Url should be available " + url ,
                UrlChecker.pingConfluence(url, 500));
    }

    @Before
    public void setUp() throws Exception {
        confluenceService.setConfluenceConfig(confluenceConfig);

    }

    @Test
    public void testFindAncestorId() throws Exception {
        confluenceService.setConfluenceConfig(confluenceConfig);
        Long id = confluenceService.findAncestorId(confluenceConfig.getSpaceKey());
        assertNotNull(id);

    }


    @Test
    public void testCreatePage() throws Exception {
        ConfluencePage page = new ConfluencePage();
        page.setTitle("temp-" + UUID.randomUUID().toString());
        page.setContent("hello");
        confluenceService.createPage(page);
    }


    @Test
    public void testAddLabel() throws Exception {
        confluenceService.addLabels(819284L, Arrays.asList("l1", "l2"));

    }

    @Test
    public void testDeleteLabel() throws Exception {
        confluenceService.deleteLabelByName(819284L, "l1");

    }

    @Test
    public void testTryToCreateErroredPage() throws Exception {

        ConfluencePage page = new ConfluencePage();
        page.setTitle("temp");
        page.setContent("{no_such_macros}");
        Long id = confluenceService.findAncestorId(confluenceConfig.getSpaceKey());
        page.setAncestorId(id);


        confluenceService.createPage(page);

    }

    @Test
    public void testParseLabels() throws Exception {
        String s = "{\"results\":[{\"prefix\":\"global\",\"name\":\"l2\",\"id\":\"1146883\"},{\"prefix\":\"global\",\"name\":\"l3\",\"id\":\"1146884\"}],\"start\":0,\"limit\":200,\"size\":2,\"_links\":{\"self\":\"http://localhost:8090/rest/api/content/819284/label\",\"base\":\"http://localhost:8090\",\"context\":\"\"}}";
        List<String> col = parseLabels(new ResponseEntity<String>(s, HttpStatus.ACCEPTED));

        assertTrue(col.size() == 2);
        assertEquals(col.get(0), "l2");
        assertEquals(col.get(1), "l3");


    }
}