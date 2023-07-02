package com.github.qwazer.markdown.confluence.core.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.qwazer.markdown.confluence.core.ConfluenceException;
import com.github.qwazer.markdown.confluence.core.HttpHeader;
import com.github.qwazer.markdown.confluence.core.NotFoundException;
import com.github.qwazer.markdown.confluence.core.model.ConfluencePage;
import com.github.qwazer.markdown.confluence.core.model.ConfluenceSpace;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Anton Reshetnikov on 24 Nov 2016.
 */
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

    private final HttpUrl baseUrl;
    private final String spaceKey;

    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;

    public ConfluenceService(
        @Nonnull final String baseUrl,
        @Nonnull final String spaceKey,
        @Nonnull final OkHttpClient httpClient
    ) {
        this(baseUrl, spaceKey, httpClient, new ObjectMapper());
    }

    public ConfluenceService(
        @Nonnull final String baseUrl,
        @Nonnull final String spaceKey,
        @Nonnull final OkHttpClient httpClient,
        @Nonnull final ObjectMapper mapper
    ) {
        this.baseUrl = HttpUrl.parse(baseUrl);
        this.spaceKey = spaceKey;
        this.httpClient = httpClient;
        this.mapper = mapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public Request getSpaceRequest(final String spaceKey) {
        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegments(String.format("space/%s", spaceKey))
            .build();

        return new Request.Builder()
            .get()
            .url(url)
            .build();
    }

    @Nullable
    public ConfluenceSpace getSpace(final String spaceKey) {
        try {
            return mapper.convertValue(executeRequest(getSpaceRequest(spaceKey)), ConfluenceSpace.class);
        } catch (IllegalArgumentException e) {
            throw new ConfluenceException(e.getMessage(), e);
        } catch (NotFoundException e) {
            return null;
        }
    }

    public ConfluenceSpace getOrCreateSpace(final String spaceKey) {
        ConfluenceSpace existingSpace = getSpace(spaceKey);
        if (existingSpace == null) {
            return createSpace(new ConfluenceSpace(spaceKey));
        } else {
            return existingSpace;
        }
    }

    public Request createSpaceRequest(final ConfluenceSpace space) {
        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegment("space")
            .build();

        return new Request.Builder()
            .url(url)
            .post(RequestBody.create(mapper.convertValue(space, JsonNode.class).toString(), MediaType.parse("application/json")))
            .build();
    }

    public ConfluenceSpace createSpace(final ConfluenceSpace space) {
        return mapper.convertValue(executeRequest(createSpaceRequest(space)), ConfluenceSpace.class);
    }

    public Request findPageByTitleRequest(final String title) {

        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegment("content")
            .setQueryParameter(SPACE_KEY, spaceKey)
            .setQueryParameter(TITLE, title)
            .setQueryParameter(EXPAND, "body.storage,version,ancestors,metadata.labels")
            .build();

        return new Request.Builder()
            .get()
            .url(url)
            .build();
    }

    @Nullable // if the page with the given title does not exist
    public ConfluencePage findPageByTitle(final String title) {
        try {
            return parseResponseEntityToConfluencePage(executeRequest(findPageByTitleRequest(title)));
        } catch (NotFoundException e) {
            LOG.debug("Page \"{}\" not found in space \"{}\"", title, spaceKey);
            return null;
        }
    }

    public Request findSpaceHomePageRequest() {

        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegment("content")
            .setQueryParameter(SPACE_KEY, spaceKey)
            .setQueryParameter(EXPAND, "body.storage,version,ancestors")
            .build();

        return new Request.Builder()
            .get()
            .url(url)
            .build();
    }

    public ConfluencePage findSpaceHomePage() {
        return parseResponseEntityToConfluencePage(executeRequest(findSpaceHomePageRequest()));
    }

    public void updatePage(final ConfluencePage page) {
        executeRequest(updatePageRequest(page));
    }

    public Request updatePageRequest(final ConfluencePage page) {
        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegments(String.format("content/%s", page.getId()))
            .build();

        final ObjectNode requestBody = buildPostBody(page);
        requestBody.put(ID, page.getId());
        requestBody.set(PAGE_VERSION, mapper.createObjectNode().put(VERSION_NUMBER, page.getVersion() + 1));

        return new Request.Builder()
            .url(url)
            .put(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
            .build();
    }

    private JsonNode executeRequest(final Request request) {
        try (final Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    throw new NotFoundException(String.format("The requested resource %s was not found", request.url()));
                }
                final String message = String.format(
                    "%s: %s",
                    response,
                    response.body() != null ? response.body().string() : "EMPTY BODY"
                );
                throw new ConfluenceException(message);
            }
            try (final ResponseBody responseBody = response.body()) {
                if (responseBody == null) {
                    throw new ConfluenceException(String.format("%s has null body", response));
                }
                return mapper.readTree(responseBody.bytes());
            } catch (IOException e) {
                throw new ConfluenceException("Could not parse Confluence REST API response", e);
            }

        } catch (IOException e) {
            throw new ConfluenceException("Could not process response", e);
        }
    }

    public Request createPageRequest(final ConfluencePage page) {

        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegment("content")
            .build();

        final ObjectNode requestBody = buildPostBody(page);
        return new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
            .build();
    }

    public Long createPage(final ConfluencePage page) {
        return parsePageIdFromResponse(executeRequest(createPageRequest(page)));
    }


    public Request addLabelsRequest(Long pageId, @Nonnull String labelName) {
        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegments(String.format("content/%d/label", pageId))
            .build();

        final JsonNode requestBody = buildAddLabelPostBody(labelName);
        return new Request.Builder()
            .url(url)
            .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
            .build();
    }

    public void addLabels(Long pageId, @Nonnull Collection<String> labels) {
        Objects.requireNonNull(labels);
        if (labels.isEmpty())
            return;

        labels.forEach(label -> executeRequest(addLabelsRequest(pageId, label)));
    }


    private JsonNode buildAddLabelPostBody(@Nonnull String labelName) {
        final ObjectNode label = mapper.createObjectNode()
            .put("name", labelName.replaceAll("\\.", "-"))
            .put("prefix", "global");
        return mapper.createArrayNode().add(label);
    }


    protected static ConfluencePage parseResponseEntityToConfluencePage(JsonNode responseBody) {
        if (responseBody == null) {
            return null;
        }

        final JsonNode results = responseBody.get("results");
        if (results != null) {
            final JsonNode first = results.get(0);
            if (first != null) {
                final ConfluencePage confluencePage = new ConfluencePage();
                confluencePage.setId(first.get("id").asLong());
                confluencePage.setVersion(first.get("version").get("number").asInt());
                confluencePage.setTitle(first.get("title").asText());

                final JsonNode bodyNode = first.get("body");
                if (bodyNode != null) {
                    final JsonNode storageNode = bodyNode.get("storage");
                    if (storageNode != null) {
                        JsonNode valueNode = storageNode.get("value");
                        if (valueNode != null) {
                            confluencePage.setContent(valueNode.asText());
                        }
                    }
                }

                final JsonNode metadataNode = first.get("metadata");
                if (metadataNode != null) {
                    final JsonNode labelsNode = metadataNode.get("labels");
                    if (labelsNode != null) {
                        final List<String> labels = labelsNode.findValues("name").stream()
                            .map(JsonNode::asText)
                            .collect(Collectors.toList());
                        confluencePage.setLabels(labels);
                    }
                }

                final JsonNode ancestors = first.get("ancestors");
                if (ancestors instanceof ArrayNode) {
                    final ArrayNode arrayNode = (ArrayNode) ancestors;
                    if (!arrayNode.isEmpty()) {
                        final Long ancestorId = arrayNode.get(arrayNode.size() - 1).get(ID).asLong();
                        LOG.debug("ancestors: {} : {}, choose -> {}", ancestors.getClass().getName(), ancestors, ancestorId);
                        confluencePage.setAncestorId(ancestorId);
                    }
                }
                return confluencePage;
            }
        }

        return null;
    }


    private ObjectNode buildPostBody(ConfluencePage confluencePage) {

        final ObjectNode spaceNode = mapper.createObjectNode();
        spaceNode.put("key", spaceKey);

        final ObjectNode storageData = mapper.createObjectNode();
        storageData.put("value", confluencePage.getContent());
        storageData.put("representation", "wiki");

        final ObjectNode storageNode = mapper.createObjectNode();
        storageNode.set("storage", storageData);

        final ObjectNode bodyNode = mapper.createObjectNode();
        bodyNode.put(TYPE, "page");
        bodyNode.put(TITLE, confluencePage.getTitle());
        bodyNode.set(SPACE, spaceNode);
        bodyNode.set(BODY, storageNode);

        if (confluencePage.getAncestorId() != null) {
            final ObjectNode ancestor = mapper.createObjectNode();
            ancestor.put("type", "page");
            ancestor.put(ID, confluencePage.getAncestorId());

            final ArrayNode ancestors = mapper.createArrayNode();
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
            LOG.info("Using page home id ({}) as ancestorId", spaceKey);
            return findSpaceHomePage().getId();
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
        final JsonNode results = responseEntity.get("results");
        if (results != null) {
            final JsonNode first = results.get(0);
            if (first != null) {
                final JsonNode id = first.get("id");
                if (id != null) {
                    return id.asText();
                }
            }
        }
        return null;
    }

    public Request getAttachmentIdRequest(Long pageId, String attachmentFilename) {
        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegments(String.format("content/%d/child/attachment", pageId))
            .addQueryParameter("filename", attachmentFilename)
            .build();

        return new Request.Builder()
            .get()
            .url(url)
            .build();
    }

    public String getAttachmentId(Long pageId, String attachmentFilename) {
        return parseAttachmentIdFromResponse(executeRequest(getAttachmentIdRequest(pageId, attachmentFilename)));
    }

    public Request createAttachmentRequest(@Nonnull Long pageId, @Nonnull String filePath) {
        Objects.requireNonNull(pageId);
        Objects.requireNonNull(filePath);
        final Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegments(String.format("content/%d/child/attachment", pageId))
            .build();

        final RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                path.getFileName().toString(),
                RequestBody.create(path.toFile(), MediaType.parse("application/octet-stream"))
            )
            .build();

        return new Request.Builder()
            .url(url)
            .header(HttpHeader.X_ATLASSIAN_TOKEN, "no-check")
            .post(requestBody)
            .build();
    }

    public void createAttachment(Long pageId, String filePath) {
        executeRequest(createAttachmentRequest(pageId, filePath));
    }

    public Request updateAttachmentRequest(@Nonnull Long pageId, @Nonnull String attachmentId, @Nonnull String filePath) {
        Objects.requireNonNull(pageId);
        Objects.requireNonNull(attachmentId);
        Objects.requireNonNull(filePath);

        final Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        final RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                path.getFileName().toString(),
                RequestBody.create(path.toFile(), MediaType.parse("application/octet-stream"))
            )
            .build();

        final HttpUrl url = baseUrl
            .newBuilder()
            .addPathSegments(String.format("content/%d/child/attachment/%s/data", pageId, attachmentId))
            .build();

        return new Request.Builder()
            .url(url)
            .header(HttpHeader.X_ATLASSIAN_TOKEN, "no-check")
            .post(requestBody)
            .build();
    }

    public void updateAttachment(Long pageId, String attachmentId, String filePath) {
        executeRequest(updateAttachmentRequest(pageId, attachmentId, filePath));
    }

}
