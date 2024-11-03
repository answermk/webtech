package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.config.OAuthConfig;
import com.webtech.rail.rail.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class AuthController {
    @Autowired
    private OAuthConfig config;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/login/oauth2/{provider}")
    public ResponseEntity<String> initiateLogin(@PathVariable String provider) {
        String authorizationUrl = buildAuthorizationUrl(provider);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizationUrl))
                .build();
    }

    @GetMapping("/callback/{provider}")
    public ResponseEntity<UserInfo> handleCallback(
            @PathVariable String provider,
            @RequestParam("code") String code
    ) {
        String accessToken = exchangeCodeForToken(code, provider);
        UserInfo userInfo = fetchUserDetails(accessToken, provider);
        return ResponseEntity.ok(userInfo);
    }

    private String buildAuthorizationUrl(String provider) {
        String encodedRedirectUri = URLEncoder.encode("http://localhost:8080/callback/" + provider, StandardCharsets.UTF_8);

        switch (provider.toLowerCase()) {
            case "google":
                return String.format(
                        "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=openid%%20email%%20profile",
                        config.getGoogleClientId(), encodedRedirectUri
                );
            case "twitter":
                return String.format(
                        "https://twitter.com/i/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=users.read%%20tweet.read",
                        config.getTwitterClientId(), encodedRedirectUri
                );
            case "facebook":
                return String.format(
                        "https://www.facebook.com/v18.0/dialog/oauth?client_id=%s&redirect_uri=%s&response_type=code&scope=email%%20public_profile",
                        config.getFacebookClientId(), encodedRedirectUri
                );
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private String exchangeCodeForToken(String code, String provider) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        switch (provider.toLowerCase()) {
            case "google":
                return exchangeGoogleToken(code, headers);
            case "twitter":
                return exchangeTwitterToken(code, headers);
            case "facebook":
                return exchangeFacebookToken(code, headers);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private String exchangeGoogleToken(String code, HttpHeaders headers) {
        String tokenUrl = config.getGoogleTokenUrl();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.getGoogleClientId());
        body.add("client_secret", config.getGoogleClientSecret());
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "http://localhost:8080/callback/google");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private String exchangeTwitterToken(String code, HttpHeaders headers) {
        String tokenUrl = config.getTwitterTokenUrl();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.getTwitterClientId());
        body.add("client_secret", config.getTwitterClientSecret());
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "http://localhost:8080/callback/twitter");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private String exchangeFacebookToken(String code, HttpHeaders headers) {
        String tokenUrl = config.getFacebookTokenUrl();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.getFacebookClientId());
        body.add("client_secret", config.getFacebookClientSecret());
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "http://localhost:8080/callback/facebook");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private UserInfo fetchUserDetails(String accessToken, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return fetchGoogleUserDetails(accessToken);
            case "twitter":
                return fetchTwitterUserDetails(accessToken);
            case "facebook":
                return fetchFacebookUserDetails(accessToken);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    private UserInfo fetchGoogleUserDetails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                config.getGoogleUserInfoUrl(),
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, String> userDetails = response.getBody();
        return new UserInfo(
                userDetails.get("sub"),
                userDetails.get("name"),
                userDetails.get("email"),
                "google"
        );
    }

    private UserInfo fetchTwitterUserDetails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                config.getTwitterUserInfoUrl(),
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, String> userDetails = response.getBody();
        return new UserInfo(
                userDetails.get("id"),
                userDetails.get("name"),
                null, // Twitter might not provide email directly
                "twitter"
        );
    }

    private UserInfo fetchFacebookUserDetails(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                config.getFacebookUserInfoUrl(),
                HttpMethod.GET,
                request,
                Map.class
        );

        Map<String, String> userDetails = response.getBody();
        return new UserInfo(
                userDetails.get("id"),
                userDetails.get("name"),
                userDetails.get("email"),
                "facebook"
        );
    }
}