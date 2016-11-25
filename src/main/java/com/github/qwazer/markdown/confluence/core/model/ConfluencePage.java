package com.github.qwazer.markdown.confluence.core.model;

import com.github.qwazer.markdown.confluence.core.ConfluenceConfig;

import java.util.Collection;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class ConfluencePage {

    private Long ancestorId;
    private String title;
    private String id;
    private Integer version;
    private String content;
    private Collection<String> labels;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        this.labels = labels;
    }
}