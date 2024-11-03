package com.webtech.rail.rail.model;


public class UserInfo {
    private String id;
    private String name;
    private String email;
    private String provider;

    public UserInfo() {}

    public UserInfo(String id, String name, String email, String provider) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.provider = provider;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}