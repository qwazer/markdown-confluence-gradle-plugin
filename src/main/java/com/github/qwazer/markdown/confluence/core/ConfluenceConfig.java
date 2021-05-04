package com.github.qwazer.markdown.confluence.core;


import groovy.lang.Closure;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Anton Reshetnikov on 10 Nov 2016.
 */
public class ConfluenceConfig {

    public static final long DEFAULT_PARSE_TIMEOUT = 2000L;

    private String authentication;
    private String restApiUrl;
    private String spaceKey;
    private boolean sslTrustAll = false;
    private Map<String,String> pageVariables;
    private Long parseTimeout = DEFAULT_PARSE_TIMEOUT;

    private Collection<Page> pages;


    public String getSpaceKey() {
        return spaceKey;
    }

    public void setSpaceKey(String spaceKey) {
        this.spaceKey = spaceKey;
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

    public String getRestApiUrl() {
        return restApiUrl;
    }

    public void setRestApiUrl(String restApiUrl) {
        this.restApiUrl = restApiUrl;
    }

    public Map<String, String> getPageVariables() {
        return pageVariables;
    }

    public void setPageVariables(Map<String, String> pageVariables) {
        this.pageVariables = pageVariables;
    }

    public Collection<Page> getPages() {
        return pages;
    }

    public void setPages(Collection<Page> pages) {
        this.pages = pages;
    }


    void pages(Closure configureClosure){

        PagesHolder pagesHolder = new PagesHolder();

        ConfigureUtil.configure(configureClosure, pagesHolder );

        this.pages = pagesHolder.getPages();
    }

    public void setParseTimeout(long parseTimeout) {
        this.parseTimeout = parseTimeout;
    }

    public long getParseTimeout() {
        return this.parseTimeout;
    }


    public static class PagesHolder {

        private final Collection<Page> pages;

        public PagesHolder() {
            pages = new ArrayList<>();
        }

        public Collection<Page> getPages() {
            return pages;
        }

        void page(Closure pageClosure){
            pages.add(ConfigureUtil.configure(pageClosure, new Page()));
        }

    }

    public static class Page{
        private String parentTitle;
        private String title;
        private File srcFile;
        private Collection<String> labels;

        public String getParentTitle() {
            return parentTitle;
        }

        public void setParentTitle(String parentTitle) {
            this.parentTitle = parentTitle;
        }

        public File getSrcFile() {
            return srcFile;
        }

        public void setSrcFile(File srcFile) {
            this.srcFile = srcFile;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Collection<String> getLabels() {
            return labels;
        }

        public void setLabels(Collection<? extends CharSequence> labels) {
            this.labels = new ArrayList<String>();
            for (CharSequence charSequence : labels){
                if (charSequence!=null) {
                    this.labels.add(charSequence.toString());
                }
            }
        }

        public void title(Object title){
            if (title!=null) {
                this.title = title.toString();
            }
        }

        public void parentTitle(Object parentTitle){
            if (parentTitle!=null) {
                this.parentTitle = parentTitle.toString();
            }
        }

        public void srcFile(File baseFile) {
            this.srcFile = baseFile;
        }

        @Override
        public String toString() {
            return "Page{" +
                    "parentTitle='" + parentTitle + '\'' +
                    ", title='" + title + '\'' +
                    ", srcFile=" + srcFile +
                    ", labels=" + labels +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ConfluenceConfig{" +
                "authentication='" + authentication + '\'' +
                ", restApiUrl='" + restApiUrl + '\'' +
                ", spaceKey='" + spaceKey + '\'' +
                ", sslTrustAll=" + sslTrustAll +
                ", parseTimeout=" + parseTimeout +
                ", pageVariables=" + pageVariables +
                ", pages=" + pages +
                '}';
    }
}
