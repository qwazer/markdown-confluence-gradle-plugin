package com.github.qwazer.markdown.confluence.core;


import java.io.File;

/**
 * Created by Anton Reshetnikov on 10 Nov 2016.
 */
public class ConfluenceConfig {

    private String parentPage;
    private String authentication;
    private String confluenceRestApiUrl;
    private File baseFile;
    private String spaceKey;
    private String title;
    private boolean sslTrustAll = false;


    public File getBaseFile() {
        return baseFile;
    }

    public void setBaseFile(File baseFile) {
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

    public String getParentPage() {
        return parentPage;
    }

    public void setParentPage(String parentPage) {
        this.parentPage = parentPage;
    }
}
