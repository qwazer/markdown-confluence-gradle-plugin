package com.github.qwazer.markdown.confluence.core;


import groovy.lang.Closure;
import org.gradle.util.ConfigureUtil;

import java.io.File;
import java.util.*;

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
    private Map<String,String> pageVariables;

    private Collection<Page> pages;



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
        private String parentPage;
        private File baseFile;
        private String title;

        public String getParentPage() {
            return parentPage;
        }

        public void setParentPage(String parentPage) {
            this.parentPage = parentPage;
        }

        public File getBaseFile() {
            return baseFile;
        }

        public void setBaseFile(File baseFile) {
            this.baseFile = baseFile;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void title(Object title){
            if (title!=null) {
                this.title = title.toString();
            }
        }

        public void parentPage(Object parentPage){
            if (parentPage!=null) {
                this.parentPage = parentPage.toString();
            }
        }

        public void baseFile(File baseFile) {
            this.baseFile = baseFile;
        }

        @Override
        public String toString() {
            return "Page{" +
                    "parentPage='" + parentPage + '\'' +
                    ", baseFile=" + baseFile +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ConfluenceConfig{" +
                "parentPage='" + parentPage + '\'' +
                ", authentication='" + authentication + '\'' +
                ", confluenceRestApiUrl='" + confluenceRestApiUrl + '\'' +
                ", baseFile=" + baseFile +
                ", spaceKey='" + spaceKey + '\'' +
                ", title='" + title + '\'' +
                ", sslTrustAll=" + sslTrustAll +
                ", pageVariables=" + pageVariables +
                ", pages=" + pages +
                '}';
    }
}
