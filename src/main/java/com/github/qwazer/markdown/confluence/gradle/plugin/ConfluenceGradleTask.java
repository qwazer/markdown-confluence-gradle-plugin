/*
 * Copyright 2016 Aaron Knight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.qwazer.markdown.confluence.gradle.plugin;

import com.github.qwazer.markdown.confluence.core.OkHttpUtils;
import com.github.qwazer.markdown.confluence.core.Utils;
import com.github.qwazer.markdown.confluence.core.service.AttachmentService;
import com.github.qwazer.markdown.confluence.core.service.ConfluenceService;
import com.github.qwazer.markdown.confluence.core.service.MarkdownService;
import com.github.qwazer.markdown.confluence.core.service.PageService;
import com.github.qwazer.markdown.confluence.core.ssl.SslUtil;
import okhttp3.OkHttpClient;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ConfluenceGradleTask extends DefaultTask {

    @TaskAction
    public void confluence() throws IOException {

        // obtaining plugins configuration
        final ConfluenceExtension extension =
            Objects.requireNonNull(getProject().getExtensions().findByType(ConfluenceExtension.class));

        // validating each and every page configured
        extension.getConfiguredPages().forEach(page -> {
            // the referenced markdown file must exist
            Utils.require(Files.exists(page.getSrcFile().toPath()), "File not found: " + page.getSrcFile());
            // page title and page parentTitle must not be same
            Utils.require(
                !page.getTitle().equals(page.getParentTitle()),
                String.format("Page title cannot be the same as page parent title: \"%s\"", page.getTitle())
            );
            // page title cannot be empty/blank
            Utils.require(page.isTitleSet(), "Page title cannot be blank/empty");
            // page parent's title cannot be empty/blank
            Utils.require(page.isParentTitleSet(), "Parent's title cannot be blank/empty");
        });

        final AuthenticationType authenticationType =
            extension.getAuthenticationType().getOrElse(AuthenticationType.BASIC);
        final String authentication = extension.getAuthentication().get();
        final String authorizationHeader = authenticationType.getAuthorizationHeader(authentication);
        final OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(60))
            .writeTimeout(Duration.ofSeconds(60))
            .addInterceptor(OkHttpUtils.getAuthorizationInterceptor(authorizationHeader));

        final boolean sslTrustAll = extension.getSslTrustAll().getOrElse(false);
        if (sslTrustAll) {
            httpClientBuilder
                .sslSocketFactory(SslUtil.INSECURE_SSL_CONTEXT.getSocketFactory(), SslUtil.INSECURE_TRUST_MANAGER);
            httpClientBuilder.hostnameVerifier((hostname, session) -> true);
        }

        final OkHttpClient httpClient = httpClientBuilder.build();

        final String restApiUrl;
        try {
            restApiUrl = new URL(extension.getRestApiUrl().get()).toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid restApiUrl value supplied: " + extension.getRestApiUrl().get());
        }

        final String spaceKey = extension.getSpaceKey().get();
        Utils.require(!spaceKey.trim().isEmpty(), "Confluence space key cannot be blank/empty");

        // Creating components that implement plugin's logic
        final ConfluenceService confluenceService = new ConfluenceService(restApiUrl, spaceKey, httpClient);

        final MarkdownService markdownService;
        markdownService = new MarkdownService();

        final AttachmentService attachmentService = new AttachmentService(confluenceService);
        final PageService pageService =
            new PageService(confluenceService, attachmentService, markdownService);
        final Map<String, String> pageVariables =
            extension.getPageVariables().getOrElse(Collections.emptyMap());

        // Publishing pages
        extension.getPages().forEach(page -> {
            try {
                pageService.publishWikiPage(page, pageVariables);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
