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
package com.github.qwazer.gradle.markdown.confluence.model;


public class ConfluencePageBuilder {
    private Long ancestorId;
    private String confluenceTitle;
    private Boolean exists;
    private String id;
    private Integer version;
    private String content;

    private ConfluencePageBuilder() {
    }

    public static ConfluencePageBuilder aConfluencePage() {
        return new ConfluencePageBuilder();
    }

    public ConfluencePageBuilder withAncestorId(Long ancestorId) {
        this.ancestorId = ancestorId;
        return this;
    }

    public ConfluencePageBuilder withConfluenceTitle(String confluenceTitle) {
        this.confluenceTitle = confluenceTitle;
        return this;
    }

    public ConfluencePageBuilder withExists(Boolean exists) {
        this.exists = exists;
        return this;
    }

    public ConfluencePageBuilder withId(String id) {
        this.id = id;
        return this;
    }


    public ConfluencePageBuilder withVersion(Integer version) {
        this.version = version;
        return this;
    }

    public ConfluencePageBuilder withWikiContent(String wikiContent) {
        this.content = wikiContent;
        return this;
    }

    public ConfluencePage build() {
        ConfluencePage confluencePage = new ConfluencePage();
        confluencePage.setAncestorId(ancestorId);
        confluencePage.setConfluenceTitle(confluenceTitle);
        confluencePage.setExists(exists);
        confluencePage.setId(id);
        confluencePage.setVersion(version);
        confluencePage.setContent(content);
        return confluencePage;
    }
}
