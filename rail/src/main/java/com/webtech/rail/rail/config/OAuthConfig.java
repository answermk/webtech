package com.webtech.rail.rail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthConfig {

    // Google OAuth properties
    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.token-url}")
    private String googleTokenUrl;

    @Value("${oauth.google.user-info-url}")
    private String googleUserInfoUrl;

    // Twitter OAuth properties
    @Value("${oauth.twitter.client-id}")
    private String twitterClientId;

    @Value("${oauth.twitter.client-secret}")
    private String twitterClientSecret;

    @Value("${oauth.twitter.token-url}")
    private String twitterTokenUrl;

    @Value("${oauth.twitter.user-info-url}")
    private String twitterUserInfoUrl;

    // Facebook OAuth properties
    @Value("${oauth.facebook.client-id}")
    private String facebookClientId;

    @Value("${oauth.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${oauth.facebook.token-url}")
    private String facebookTokenUrl;

    @Value("${oauth.facebook.user-info-url}")
    private String facebookUserInfoUrl;

    // Default constructor
    public OAuthConfig() {}

    // Getters for each property
    public String getGoogleClientId() {
        return googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public String getGoogleTokenUrl() {
        return googleTokenUrl;
    }

    public String getGoogleUserInfoUrl() {
        return googleUserInfoUrl;
    }

    public String getTwitterClientId() {
        return twitterClientId;
    }

    public String getTwitterClientSecret() {
        return twitterClientSecret;
    }

    public String getTwitterTokenUrl() {
        return twitterTokenUrl;
    }

    public String getTwitterUserInfoUrl() {
        return twitterUserInfoUrl;
    }

    public String getFacebookClientId() {
        return facebookClientId;
    }

    public String getFacebookClientSecret() {
        return facebookClientSecret;
    }

    public String getFacebookTokenUrl() {
        return facebookTokenUrl;
    }

    public String getFacebookUserInfoUrl() {
        return facebookUserInfoUrl;
    }
}
