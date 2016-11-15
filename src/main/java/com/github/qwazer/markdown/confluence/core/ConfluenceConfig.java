package com.github.qwazer.markdown.confluence.core;



/**
 * Created by Anton Reshetnikov on 10 Nov 2016.
 */
public class ConfluenceConfig {

    private Long ancestorId;
    private String authentication;
    private String confluenceRestApiUrl;
    private String baseDir;
    private String baseFile = "README.md";
    private String spaceKey;
    private String title;
    private boolean sslTrustAll = false;


    public Long getAncestorId() {
        return ancestorId;
    }

    public void setAncestorId(Long ancestorId) {
        this.ancestorId = ancestorId;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseFile() {
        return baseFile;
    }

    public void setBaseFile(String baseFile) {
        this.baseFile = baseFile;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSslTrustAll() {
        return sslTrustAll;
    }

    public void setSslTrustAll(boolean sslTrustAll) {
        this.sslTrustAll = sslTrustAll;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getConfluenceRestApiUrl() {
        return confluenceRestApiUrl;
    }

    public void setConfluenceRestApiUrl(String confluenceRestApiUrl) {
        this.confluenceRestApiUrl = confluenceRestApiUrl;
    }
}
