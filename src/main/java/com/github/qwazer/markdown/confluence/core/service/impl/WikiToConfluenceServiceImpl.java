package com.github.qwazer.markdown.confluence.core.service.impl;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.exception.ConfluenceAPIException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePageBuilder;
import com.github.qwazer.markdown.confluence.core.service.Wiki2ConfluenceService;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;
import static org.jsoup.nodes.Entities.EscapeMode.xhtml;

/**
 * Created by Anton Reshetnikov on 14 Nov 2016.
 */
@Service
public class WikiToConfluenceServiceImpl implements Wiki2ConfluenceService {

    private static final Logger LOG = LoggerFactory.getLogger(WikiToConfluenceServiceImpl.class);

    private static final String EXPAND = "expand";
    private static final String ID = "id";
    private static final String SPACE_KEY = "spaceKey";
    private static final String TITLE = "title";

    private static final ThreadLocal<ConfluenceConfig> CONFLUENCE_CONFIG = new ThreadLocal<>();

    private final RestTemplate restTemplate;

    @Autowired
    public WikiToConfluenceServiceImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void postWikiToConfluence(final ConfluenceConfig confluenceConfig, final String wiki) {
        LOG.info("Posting XHTML to Confluence...");

        CONFLUENCE_CONFIG.set(confluenceConfig);

        ConfluencePage confluencePage =  new ConfluencePage();
        confluencePage.setAncestorId(confluenceConfig.getAncestorId());
        confluencePage.setConfluenceTitle(confluenceConfig.getTitle());
        confluencePage.setContent(wiki);

        LOG.debug("ANCESTOR ID SET: <{}> -> {}", confluencePage.getConfluenceTitle(), confluencePage.getAncestorId());

        addExistensePageData(confluencePage);

        if (confluencePage.exists()) {
            updatePage(confluencePage);

        } else {
            createPage(confluencePage);
        }

    }


    private static Document parseXhtml(final String inputXhtml) {
        final Document originalDocument = Jsoup.parse(inputXhtml, "utf-8", Parser.xmlParser());
        originalDocument.outputSettings().prettyPrint(false);
        originalDocument.outputSettings().escapeMode(xhtml);
        originalDocument.outputSettings().charset("UTF-8");

        return originalDocument;
    }




    private void addExistensePageData(final ConfluencePage confluencePage) {
        final ConfluenceConfig confluenceConfig = CONFLUENCE_CONFIG.get();

        final HttpHeaders httpHeaders = buildHttpHeaders(confluenceConfig.getAuthentication());
        final HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getConfluenceRestApiUrl())
                .path("/content")
                .queryParam(SPACE_KEY, confluenceConfig.getSpaceKey())
                .queryParam(TITLE, confluencePage.getConfluenceTitle())
                .queryParam(EXPAND, "body.storage,version,ancestors")
                .build()
                .toUri();

        final ResponseEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.GET, requestEntity, String.class);

        final String jsonBody = responseEntity.getBody();

        try {
            LOG.debug("GET RESPONSE: {}", jsonBody);

            final String id = JsonPath.read(jsonBody, "$.results[0].id");
            final Integer version = JsonPath.read(jsonBody, "$.results[0].version.number");

            final JSONArray ancestors = JsonPath.read(jsonBody, "$.results[0].ancestors");

            if (!ancestors.isEmpty()) {
                final Map<String, Object> lastAncestor = (Map<String, Object>) ancestors.get(ancestors.size() - 1);
                final Long ancestorId = Long.valueOf((String) lastAncestor.get(ID));

                LOG.debug("ANCESTORS: {} : {}, CHOSE -> {}", ancestors.getClass().getName(), ancestors, ancestorId);
                confluencePage.setAncestorId(ancestorId);
            }

            confluencePage.setId(id);
            confluencePage.setVersion(version);
            confluencePage.setExists(true);

            LOG.info("Page <{} : {}> Already Exists, Performing an Update!", confluencePage.getId(),
                    confluencePage.getConfluenceTitle());

        } catch (final PathNotFoundException e) {
            confluencePage.setExists(false);

            LOG.info("Page <{}> Does Not Exist, Creating a New Page!", confluencePage.getConfluenceTitle());

            // Prevent New Pages from Being Orphaned if there was no ancestor id
            // specified by querying for the id of the space root. Confluence
            // does not do this automatically, and thus you would otherwise not be
            // able to navigate to the page unless you manually knew the URL
            if (confluencePage.getAncestorId() == null) {
                final ConfluencePage spaceRootPage = ConfluencePageBuilder.aConfluencePage()
                        .withConfluenceTitle(confluenceConfig.getSpaceKey())
                        .build();
                addExistensePageData(spaceRootPage);

                final Long spaceRootAncestorId = Long.valueOf(spaceRootPage.getId());

                LOG.info("ORPHAN PREVENTION FAIL SAFE: Using Space Root Ancestor Id {}",
                        spaceRootAncestorId);

                confluencePage.setAncestorId(spaceRootAncestorId);
            }
        }
    }

    private static HttpHeaders buildHttpHeaders(final String confluenceAuthentication) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", confluenceAuthentication));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return headers;
    }

    private static Long getPageIdFromResponse(final HttpEntity<String> responseEntity) {
        final String responseJson = responseEntity.getBody();
        final JSONParser jsonParser = new JSONParser(DEFAULT_PERMISSIVE_MODE);

        try {
            final JSONObject response = jsonParser.parse(responseJson, JSONObject.class);
            return Long.valueOf((String) response.get(ID));
        } catch (ParseException e) {
            throw new ConfluenceAPIException("Error Parsing JSON Response from Confluence!", e);
        }
    }

    private void createPage(final ConfluencePage page) {
        final ConfluenceConfig confluenceConfig = CONFLUENCE_CONFIG.get();
        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getConfluenceRestApiUrl())
                .path("/content")
                .build()
                .toUri();

        final HttpHeaders httpHeaders = buildHttpHeaders(confluenceConfig.getAuthentication());
      //  final String formattedXHtml = reformatXHtml(page.getContent(), confluenceLinkMap);
        final String jsonPostBody = buildPostBody(page.getAncestorId(), page.getConfluenceTitle(), page.getContent()).toJSONString();

        LOG.debug("CREATE PAGE REQUEST: {}", jsonPostBody);

        final HttpEntity<String> requestEntity = new HttpEntity<>(jsonPostBody, httpHeaders);

        final HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.POST, requestEntity, String.class);

        LOG.debug("CREATE PAGE RESPONSE: {}", responseEntity.getBody());

        final Long pageId = getPageIdFromResponse(responseEntity);
        page.setAncestorId(pageId);
    }

    private void updatePage(final ConfluencePage page) {
        final ConfluenceConfig confluenceConfig = CONFLUENCE_CONFIG.get();

        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getConfluenceRestApiUrl())
                .path(String.format("/content/%s", page.getId()))
                .build()
                .toUri();

        final HttpHeaders httpHeaders = buildHttpHeaders(confluenceConfig.getAuthentication());

        final JSONObject postVersionObject = new JSONObject();
        postVersionObject.put("number", page.getVersion() + 1);

      ///  final String formattedXHtml = reformatXHtml(page.getContent(), confluenceLinkMap);
        final JSONObject postBody = buildPostBody(page.getAncestorId(), page.getConfluenceTitle(), page.getContent());
        postBody.put(ID, page.getId());
        postBody.put("version", postVersionObject);

        final HttpEntity<String> requestEntity = new HttpEntity<>(postBody.toJSONString(), httpHeaders);

        LOG.debug("UPDATE PAGE REQUEST: {}", postBody);

        final HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl, HttpMethod.PUT, requestEntity, String.class);

        LOG.debug("UPDATE PAGE RESPONSE: {}", responseEntity.getBody());

        final Long pageId = getPageIdFromResponse(responseEntity);
        page.setAncestorId(pageId);
    }

    private static JSONObject buildPostBody(final Long ancestorId, final String confluenceTitle, final String xhtml) {
        final ConfluenceConfig confluenceConfig = CONFLUENCE_CONFIG.get();

        final JSONObject jsonSpaceObject = new JSONObject();
        jsonSpaceObject.put("key", confluenceConfig.getSpaceKey());

        final JSONObject jsonStorageObject = new JSONObject();
        jsonStorageObject.put("value", xhtml);
      //  jsonStorageObject.put("representation", "storage");
        jsonStorageObject.put("representation", "wiki");

        final JSONObject jsonBodyObject = new JSONObject();
        jsonBodyObject.put("storage", jsonStorageObject);

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "page");
        jsonObject.put(TITLE, confluenceTitle);
        jsonObject.put("space", jsonSpaceObject);
        jsonObject.put("body", jsonBodyObject);

        if (ancestorId != null) {
            final JSONObject ancestor = new JSONObject();
            ancestor.put("type", "page");
            ancestor.put(ID, ancestorId);

            final JSONArray ancestors = new JSONArray();
            ancestors.add(ancestor);

            jsonObject.put("ancestors", ancestors);
        }

        return jsonObject;
    }

//    private static String reformatXHtml(final String inputXhtml, final Map<String, ConfluenceLink> confluenceLinkMap) {
//        final Document document = Jsoup.parse(inputXhtml, "utf-8", Parser.xmlParser());
//        document.outputSettings().prettyPrint(false);
//        document.outputSettings().escapeMode(xhtml);
//        document.outputSettings().charset("UTF-8");
//
//        final Elements linkElements = document.select("a");
//
//        for (final Element linkElement : linkElements) {
//            final String originalHref = linkElement.attr("href");
//            final ConfluenceLink confluenceLink = confluenceLinkMap.get(originalHref);
//
//            if (confluenceLink == null) {
//                LOG.debug("NO LINK MAPPING FOUND TO COVERT LINK: {}", originalHref);
//                continue;
//            }
//
//            final String confluenceLinkMarkup = confluenceLink.getConfluenceLinkMarkup();
//
//            LOG.debug("LINK CONVERSION: {} -> {}", originalHref, confluenceLinkMarkup);
//
//            linkElement.before(confluenceLinkMarkup);
//
//            linkElement.html("");
//            linkElement.unwrap();
//        }
//
//        reformatXHtmlHeadings(document, "h2");
//        reformatXHtmlHeadings(document, "h3");
//        reformatXHtmlHeadings(document, "#toctitle");
//
//        final SwaggerConfluenceConfig swaggerConfluenceConfig = CONFLUENCE_CONFIG.get();
//
//        if(swaggerConfluenceConfig.getPaginationMode()==PaginationMode.SINGLE_PAGE){
//            if(swaggerConfluenceConfig.isIncludeTableOfContentsOnSinglePage()) {
//                reformatXHtmlBreakAfterElements(document, "#toc");
//            }
//
//            reformatXHtmlBreakAfterElements(document, ".sect1");
//        }
//
//        reformatXHtmlSpacing(document.select(".sect2"));
//        reformatXHtmlSpacing(document.select(".sect3"));
//
//        return document.html();
//    }
//
//    private static void reformatXHtmlHeadings(final Document document, final String selector){
//        final Elements elements = document.select(selector);
//
//        for(final Element element : elements){
//            final String text = element.text();
//            final String strongHeaderText = String.format("<strong>%s</strong>", text);
//            element.html(strongHeaderText);
//        }
//    }
//
//    private static void reformatXHtmlBreakAfterElements(final Document document, final String elements){
//        document.select(elements).after("<br />");
//    }
//
//    private static void reformatXHtmlSpacing(final Elements elements){
//        for(final Element element : elements){
//            element.before("<br />");
//        }
//    }


}