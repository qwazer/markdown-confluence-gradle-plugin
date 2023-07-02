package com.github.qwazer.markdown.confluence.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class ConfluencePage {

    private Long ancestorId;
    private String title;
    private Long id;
    private Integer version;
    private String content;
    private Collection<String> labels = new ArrayList<>();

    public Long getAncestorId() {
        return ancestorId;
    }

    public void setAncestorId(Long ancestorId) {
        this.ancestorId = ancestorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        Objects.requireNonNull(labels);
        this.labels = labels;
    }

}