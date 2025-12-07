package com.kodilla.travelplannerfront.security;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private String username;
    private String password;

    public void authenticate(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public boolean isAuthenticated() {
        return username != null && password != null;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void logout() {
        this.username = null;
        this.password = null;
    }
}
