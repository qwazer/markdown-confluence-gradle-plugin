package com.github.qwazer.markdown.confluence.core.model;

/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
public class ConfluencePage {

    private Long ancestorId;
    private String confluenceTitle;
    private Boolean exists;
    private String id;
    private Integer version;
    private String content;

    public Long getAncestorId() {
        return ancestorId;
    }

    public void setAncestorId(Long ancestorId) {
        this.ancestorId = ancestorId;
    }

    public String getConfluenceTitle() {
        return confluenceTitle;
    }

    public void setConfluenceTitle(String confluenceTitle) {
        this.confluenceTitle = confluenceTitle;
    }

    public Boolean exists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
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

}