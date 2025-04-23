package com.github.qwazer.markdown.confluence.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ConfluenceExtension {

    public abstract Property<AuthenticationType> getAuthenticationType();
    public abstract Property<String> getAuthentication();
    public abstract Property<String> getRestApiUrl();
    public abstract Property<String> getSpaceKey();
    public abstract Property<Boolean> getSslTrustAll();
    public abstract MapProperty<String, String> getPageVariables();
    abstract Property<String> getAuthenticationTypeString();
    abstract NamedDomainObjectContainer<Page> getConfiguredPages();
    public abstract Property<String> getParserType();

    private List<Page> pages;

    @SuppressWarnings("unused") // used by Gradle
    public void pages(Action<? super NamedDomainObjectContainer<Page>> action) {
        action.execute(getConfiguredPages());
    }

    /**
     * When later creating the pages, we need to created parent pages first before we create child pages.
     *
     * @return Pages ordered such that all parents are before the children.
     */
    public List<Page> getPages() {
        if (pages == null) {
            pages = getPages(new ArrayList<>(getConfiguredPages()));
        }
        return pages;
    }

    private List<Page> getPages(List<Page> pages) {
        if (pages.isEmpty()) {
            return Collections.emptyList();
        }
        return pages.stream()
                .collect(Collectors.partitioningBy(page -> hasParent(page, pages)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(entry -> {
                    if (entry.getKey()) { // pages with parent
                        return getPages(entry.getValue()).stream();
                    } else { // pages with no parent
                        return entry.getValue().stream();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * @return {@code true} if {@code page} has parent in {@code pages}.
     */
    boolean hasParent(Page page, List<Page> pages) {
        return pages.stream()
                .anyMatch(pageFromPages -> pageFromPages != page && page.getParentTitle().equals(pageFromPages.getName()));
    }

    public static abstract class Page implements Named {

        private final String name;
        private String parentTitle;
        private File srcFile;
        private List<String> labels = new ArrayList<>();

        public Page(String name) {
            this.name = name;
        }

        public String getContent() throws IOException {
            return new String(Files.readAllBytes(getSrcFile().toPath()), StandardCharsets.UTF_8);
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @NotNull
        public String getTitle() {
            return getName();
        }

        public boolean isTitleSet() {
            return !getTitle().trim().isEmpty();
        }

        public String getParentTitle() {
            return parentTitle;
        }

        public void setParentTitle(String parentTitle) {
            this.parentTitle = parentTitle;
        }

        public boolean isParentTitleSet() {
            final String parentTitle = getParentTitle();
            return parentTitle != null && !parentTitle.trim().isEmpty();
        }

        public File getSrcFile() {
            return srcFile;
        }

        public void setSrcFile(File srcFile) {
            this.srcFile = srcFile;
        }

        public List<String> getLabels() {
            return labels;
        }

        // for some reason unknown to me, Gradle may pass a list of GString object here, which are not Java Strings
        // and then ClassCastException may be thrown.
        public void setLabels(List<Object> labels) {
            if (labels == null) {
                throw new IllegalArgumentException("labels cannot be null");
            }
            this.labels = labels.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

    }

}
