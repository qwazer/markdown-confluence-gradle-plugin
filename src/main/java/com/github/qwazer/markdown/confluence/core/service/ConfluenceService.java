package com.github.qwazer.markdown.confluence.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
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

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
@Service
public class ConfluenceService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfluenceService.class);

    private static final String BODY = "body";
    private static final String EXPAND = "expand";
    private static final String ID = "id";
    private static final String SPACE = "space";
    private static final String SPACE_KEY = "spaceKey";
    private static final String TITLE = "title";
    private static final String TYPE = "type";
    private static final String PAGE_VERSION = "version";
    private static final String VERSION_NUMBER = "number";

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
        this.httpHeaders = buildHttpHeaders(confluenceConfig);
        this.httpHeadersForAttachment = buildHttpHeadersForAttachment(confluenceConfig);
    }

    private static HttpHeaders buildHttpHeaders(final ConfluenceConfig config) {
        final var headers = new HttpHeaders();
        headers.set("Authorization", config.getAuthenticationType().getAuthorizationHeader(config));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private static HttpHeaders buildHttpHeadersForAttachment(final ConfluenceConfig config) {
        final var headers = new HttpHeaders();
        headers.set("Authorization", config.getAuthenticationType().getAuthorizationHeader(config));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
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

        final ResponseEntity<JsonNode> responseEntity =
                restTemplate.exchange(targetUrl, HttpMethod.GET, requestEntity, JsonNode.class);

        return parseResponseEntityToConfluencePage(responseEntity.getBody());
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

        final var responseBody = Objects.requireNonNull(
                restTemplate
                    .exchange(targetUrl, HttpMethod.GET, requestEntity, JsonNode.class)
                    .getBody()
            );
        return parseResponseEntityToConfluencePage(responseBody);
    }


    public void updatePage(final ConfluencePage page) {

        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path(String.format("/content/%s", page.getId()))
                .build()
                .toUri();

        final var pageVersion = new ObjectNode(JsonNodeFactory.instance);
        pageVersion.put(VERSION_NUMBER, page.getVersion() + 1);

        final var requestBody = buildPostBody(page);
        requestBody.put(ID, page.getId());
        requestBody.set(PAGE_VERSION, pageVersion);

        LOG.debug("Update page request: {}", requestBody);

        final var requestEntity = new HttpEntity<>(requestBody, httpHeaders);
        final var responseBody = Objects.requireNonNull(
                restTemplate
                    .exchange(targetUrl, HttpMethod.PUT, requestEntity, JsonNode.class)
                    .getBody()
            );

        LOG.debug("Update page response: {}", responseBody);

    }

    public Long createPage(final ConfluencePage page) {
        final URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content")
                .build()
                .toUri();

        final String jsonPostBody = buildPostBody(page).toPrettyString();

        LOG.debug("Create page request: {}", jsonPostBody);

        final var requestEntity = new HttpEntity<>(jsonPostBody, httpHeaders);

        final var responseBody = Objects.requireNonNull(restTemplate
                .postForEntity(targetUrl, requestEntity, JsonNode.class)
                .getBody());

        LOG.debug("Create page response: {}", responseBody.toPrettyString());

        return parsePageIdFromResponse(responseBody);
    }


    public void addLabels(Long pageId, @Nonnull Collection<String> labels) {
        if (labels.isEmpty())
            return;

        URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content/{id}/label")
                .buildAndExpand(pageId)
                .toUri();

        final var jsonPostBody = buildAddLabelsPostBody(labels);

        LOG.debug("Add labels request: {}", jsonPostBody);
        final var requestEntity = new HttpEntity<>(jsonPostBody, httpHeaders);
        final var responseBody = Objects.requireNonNull(
            restTemplate
                .postForEntity(targetUrl, requestEntity, JsonNode.class)
                .getBody()
        );
        LOG.debug("Response of adding labels: {}", responseBody);
    }


    private static ArrayNode buildAddLabelsPostBody(@Nonnull Collection<String> labels) {
        var arrayNode = new ArrayNode(JsonNodeFactory.instance);
        for (String s : labels) {
            if (s != null && !s.isEmpty()) {
                var label = new ObjectNode(JsonNodeFactory.instance);
                label.put("prefix", "global");
                label.put("name", s);
                arrayNode.add(label);
            }
        }
        return arrayNode;
    }

    protected static ConfluencePage parseResponseEntityToConfluencePage(JsonNode responseBody) {
        if (responseBody == null) {
            return null;
        }

        final var results = responseBody.get("results");
        if (results != null) {
            final var first = results.get(0);
            if (first != null) {
                final var confluencePage = new ConfluencePage();
                confluencePage.setId(first.get("id").asLong());
                confluencePage.setVersion(first.get("version").get("number").asInt());

                final var ancestors = first.get("ancestors");
                if (ancestors instanceof ArrayNode arrayNode && !arrayNode.isEmpty()) {
                    final var ancestorId = arrayNode.get(arrayNode.size() - 1).get(ID).asLong();
                    LOG.debug("ancestors: {} : {}, choose -> {}", ancestors.getClass().getName(), ancestors, ancestorId);
                    confluencePage.setAncestorId(ancestorId);
                }
                return confluencePage;
            }
        }

        return null;
    }

    private ObjectNode buildPostBody(ConfluencePage confluencePage) {

        final var spaceNode = new ObjectNode(JsonNodeFactory.instance);
        spaceNode.put("key", confluenceConfig.getSpaceKey());

        final var storageData = new ObjectNode(JsonNodeFactory.instance);
        storageData.put("value", confluencePage.getContent());
        storageData.put("representation", "wiki");

        final var storageNode = new ObjectNode(JsonNodeFactory.instance);
        storageNode.set("storage", storageData);

        final var bodyNode = new ObjectNode(JsonNodeFactory.instance);
        bodyNode.put(TYPE, "page");
        bodyNode.put(TITLE, confluencePage.getTitle());
        bodyNode.set(SPACE, spaceNode);
        bodyNode.set(BODY, storageNode);

        if (confluencePage.getAncestorId() != null) {
            final var ancestor = new ObjectNode(JsonNodeFactory.instance);
            ancestor.put("type", "page");
            ancestor.put(ID, confluencePage.getAncestorId());

            final var ancestors = new ArrayNode(JsonNodeFactory.instance);
            ancestors.add(ancestor);

            bodyNode.set("ancestors", ancestors);
        }

        return bodyNode;
    }

    public Long findAncestorId(String title) {
        LOG.info("Looking up ancestor id by title {}", title);
        ConfluencePage page = findPageByTitle(title);
        if (page != null) {
            return page.getId();
        } else {
            LOG.info("Using page home id ({}) as ancestorId", confluenceConfig.getSpaceKey());
            ConfluencePage spaceHome = findSpaceHomePage(confluenceConfig.getSpaceKey());
            return spaceHome.getId();
        }
    }

    private static Long parsePageIdFromResponse(final JsonNode responseEntity) {
        try {
            return responseEntity.get(ID).asLong();
        } catch (Exception e) {
            throw new ConfluenceException("Error Parsing JSON Response from Confluence!", e);
        }
    }

    private static String parseAttachmentIdFromResponse(final JsonNode responseEntity) {
        final var results = responseEntity.get("results");
        if (results != null) {
            final var first = results.get(0);
            if (first != null) {
                final var id = first.get("id");
                if (id != null) {
                    return id.asText();
                }
            }
        }
        return null;
    }

    public String getAttachmentId(Long pageId, String attachmentFilename) {

        final HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        URI targetUrl = UriComponentsBuilder.fromUriString(confluenceConfig.getRestApiUrl())
                .path("/content/{id}/child/attachment")
                .queryParam("filename", attachmentFilename)
                .buildAndExpand(pageId)
                .toUri();

        final var responseBody = Objects.requireNonNull(
            restTemplate
                .exchange(targetUrl, HttpMethod.GET, requestEntity, JsonNode.class)
                .getBody()
        );

        LOG.debug("Create attachment response: {}", responseBody.toPrettyString());

        return parseAttachmentIdFromResponse(responseBody);
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

    /*
     * Package-private access so that it's visible in tests.
     */
    HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }
}
