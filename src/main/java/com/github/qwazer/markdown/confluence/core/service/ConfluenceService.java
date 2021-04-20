package com.github.qwazer.markdown.confluence.core.service;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static net.minidev.json.parser.JSONParser.DEFAULT_PERMISSIVE_MODE;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
@Service
public class ConfluenceService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfluenceService.class);

    private static final String EXPAND = "expand";
    private static final String ID = "id";
    private static final String SPACE_KEY = "spaceKey";
    private static final String TITLE = "title";

    private final RestTemplate restTemplate;

    private ConfluenceConfig confluenceConfig;
    private HttpHeaders httpHeaders;
    private HttpHeaders httpHeadersForAttachment;

    @Autowired
    public ConfluenceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setConfluenceConfig(ConfluenceConfig confluenceConfig) {
        this.confluenceConfig = confluenceConfig;
        httpHeaders = buildHttpHeaders(confluenceConfig.getAuthentication());
        httpHeadersForAttachment = buildHttpHeadersForAttachment(confluenceConfig.getAuthentication());
    }

    private static HttpHeaders buildHttpHeaders(final String confluenceAuthentication) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", confluenceAuthentication));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        return headers;
    }

    private static HttpHeaders buildHttpHeadersForAttachment(final String confluenceAuthentication) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Basic %s", confluenceAuthentication));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Atlassian-Token", "no-check");

        return headers;
    }


    public ConfluencePage findPageByTitle(final String title) {

        final HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content")
                .queryParam(SPACE_KEY, confluenceConfig.getSpaceKey())
                .queryParam(TITLE, title)
                .queryParam(EXPAND, "body.storage,version,ancestors")
                .build(false)
                .encode()
                .toUri();

        final ResponseEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.GET, requestEntity, String.class);

        ConfluencePage confluencePage = parseResponseEntityToConfluencePage(responseEntity);
        return confluencePage;
    }

    public ConfluencePage findSpaceHomePage(final String spaceKey) {

        final HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content")
                .queryParam(SPACE_KEY, confluenceConfig.getSpaceKey())
                .queryParam(EXPAND, "body.storage,version,ancestors")
                .build(false)
                .encode()
                .toUri();

        final ResponseEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.GET, requestEntity, String.class);

        ConfluencePage confluencePage = parseResponseEntityToConfluencePage(responseEntity);
        return confluencePage;
    }


    public void updatePage(final ConfluencePage page) {

        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path(String.format("/content/%s", page.getId()))
                .build()
                .toUri();


        final JSONObject postVersionObject = new JSONObject();
        postVersionObject.put("number", page.getVersion() + 1);

        final JSONObject postBody = buildPostBody(page);
        postBody.put(ID, page.getId());
        postBody.put("version", postVersionObject);

        final HttpEntity<String> requestEntity = new HttpEntity<>(postBody.toJSONString(), httpHeaders);

        LOG.debug("Request of updating page: {}", postBody);

        final HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl, HttpMethod.PUT, requestEntity, String.class);

        LOG.debug("Response of updating page: {}", responseEntity.getBody());

    }

    public Long createPage(final ConfluencePage page) {
        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content")
                .build()
                .toUri();

        final String jsonPostBody = buildPostBody(page).toJSONString();

        LOG.debug("Request of creating page: {}", jsonPostBody);

        final HttpEntity<String> requestEntity = new HttpEntity<>(jsonPostBody, httpHeaders);

        final HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.POST, requestEntity, String.class);

        LOG.debug("Response of creating page: {}", responseEntity.getBody());

        return parsePageIdFromResponse(responseEntity);
    }


    public void addLabels(Long pageId, Collection<String> labels) {
        if (labels == null || labels.isEmpty())
            return;

        URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content/{id}/label")
                .buildAndExpand(pageId)
                .toUri();

        final String jsonPostBody = buildAddLabelsPostBody(labels);

        LOG.debug("Request of adding labels: {}", jsonPostBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonPostBody, httpHeaders);
        HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.POST, requestEntity, String.class);
        LOG.debug("Response of adding labels: {}", responseEntity.getBody());

    }


    private static String buildAddLabelsPostBody(Collection<String> labels) {
        if (labels == null || labels.isEmpty()) return null;
        JSONArray jsonArray = new JSONArray();
        for (String s : labels) {
            if (s != null && !s.isEmpty()) {
                JSONObject label = new JSONObject();
                label.put("prefix", "global");
                label.put("name", s);
                jsonArray.add(label);
            }
        }
        return jsonArray.toJSONString();
    }


    protected static ConfluencePage parseResponseEntityToConfluencePage(ResponseEntity<String> responseEntity) {
        final String jsonBody = responseEntity.getBody();

        try {
            LOG.debug("Try to parse response: {}", jsonBody);

            final String id = JsonPath.read(jsonBody, "$.results[0].id");
            final Integer version = JsonPath.read(jsonBody, "$.results[0].version.number");

            final JSONArray ancestors = JsonPath.read(jsonBody, "$.results[0].ancestors");

            ConfluencePage confluencePage = new ConfluencePage();

            if (!ancestors.isEmpty()) {
                final Map<String, Object> lastAncestor = (Map<String, Object>) ancestors.get(ancestors.size() - 1);
                final Long ancestorId = Long.valueOf((String) lastAncestor.get(ID));

                LOG.debug("ancestors: {} : {}, choose -> {}", ancestors.getClass().getName(), ancestors, ancestorId);


                confluencePage.setAncestorId(ancestorId);
            }

            confluencePage.setId(Long.parseLong(id));
            confluencePage.setVersion(version);

            return confluencePage;

        } catch (final PathNotFoundException e) {
            return null;
        }
    }


    private JSONObject buildPostBody(ConfluencePage confluencePage) {

        final JSONObject jsonSpaceObject = new JSONObject();
        jsonSpaceObject.put("key", confluenceConfig.getSpaceKey());

        final JSONObject jsonStorageObject = new JSONObject();
        jsonStorageObject.put("value", confluencePage.getContent());
        //  jsonStorageObject.put("representation", "storage");
        jsonStorageObject.put("representation", "wiki");

        final JSONObject jsonBodyObject = new JSONObject();
        jsonBodyObject.put("storage", jsonStorageObject);

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "page");
        jsonObject.put(TITLE, confluencePage.getTitle());
        jsonObject.put("space", jsonSpaceObject);
        jsonObject.put("body", jsonBodyObject);

        if (confluencePage.getAncestorId() != null) {
            final JSONObject ancestor = new JSONObject();
            ancestor.put("type", "page");
            ancestor.put(ID, confluencePage.getAncestorId());

            final JSONArray ancestors = new JSONArray();
            ancestors.add(ancestor);

            jsonObject.put("ancestors", ancestors);
        }

        return jsonObject;
    }

    public Long findAncestorId(String title) {
        LOG.info("Try to find ancestorId by title {}", title);

        ConfluencePage page = findPageByTitle(title);
        if (page != null) {
            return page.getId();
        } else {
            LOG.info("Try to use page home id as ancestorId", confluenceConfig.getSpaceKey());

            ConfluencePage spaceHome = findSpaceHomePage(confluenceConfig.getSpaceKey());
            return spaceHome.getId();
        }
    }

    private static Long parsePageIdFromResponse(final HttpEntity<String> responseEntity) {
        final String responseJson = responseEntity.getBody();
        final JSONParser jsonParser = new JSONParser(DEFAULT_PERMISSIVE_MODE);

        try {
            final JSONObject response = jsonParser.parse(responseJson, JSONObject.class);
            return Long.valueOf((String) response.get(ID));
        } catch (ParseException e) {
            throw new ConfluenceException("Error Parsing JSON Response from Confluence!", e);
        }
    }

    private static String parseAttachmentIdFromResponse(final HttpEntity<String> responseEntity) {
        final String jsonBody = responseEntity.getBody();

        try {
            return JsonPath.read(jsonBody, "$.results[0].id");
        } catch (final PathNotFoundException e) {
            return null;
        }
    }

    public String getAttachmentId(Long pageId, String attachmentFilename) {

        final HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content/{id}/child/attachment")
                .queryParam("filename", attachmentFilename)
                .buildAndExpand(pageId)
                .toUri();

        final HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.GET, requestEntity, String.class);

        LOG.debug("Response of creating attachment: {}", responseEntity.getBody());

        return parseAttachmentIdFromResponse(responseEntity);
    }

    public void createAttachment(Long pageId, String filePath) {
        URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content/{id}/child/attachment")
                .buildAndExpand(pageId)
                .toUri();
        postAttachment(targetUrl, filePath);
    }

    public void updateAttachment(Long pageId, String attachmentId, String filePath) {
        URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content/{pageId}/child/attachment/{attachmentId}/data")
                .buildAndExpand(pageId, attachmentId)
                .toUri();
        postAttachment(targetUrl, filePath);
    }

    public void postAttachment(URI targetUrl, String filePath) {
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));

        HttpEntity<MultiValueMap<String, Object>> multiValueMapHttpEntity = new HttpEntity<>(body, httpHeadersForAttachment);
        HttpEntity<String> responseEntity = restTemplate.exchange(targetUrl,
                HttpMethod.POST, multiValueMapHttpEntity, String.class);
        LOG.debug("Response of adding attachment: {}", responseEntity.getBody());
    }
}
