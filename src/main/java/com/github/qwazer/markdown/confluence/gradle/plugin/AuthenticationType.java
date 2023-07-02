package com.github.qwazer.markdown.confluence.gradle.plugin;

/**
 * Authentication strategy to use when interacting with Confluence APIs.
 */
public enum AuthenticationType {
    /**
     * Use username/password combination (a.k.a. basic authentication) to authenticate Confluence API calls.
     */
    BASIC("Basic"),
    /**
     * Use Personal Access Token (PAT) to authenticate Confluence API calls.
     */
    PAT("Bearer");

    private final String authorizationHeaderPrefix;

    AuthenticationType(String authorizationHeaderPrefix) {
        this.authorizationHeaderPrefix = authorizationHeaderPrefix;
    }

    public String getAuthorizationHeader(String authentication) {
        return String.format("%s %s", authorizationHeaderPrefix, authentication);
    }
}
