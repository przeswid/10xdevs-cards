package com.ten.devs.cards.cards.user.domain;

import java.util.List;
import java.util.Optional;

public class UserSpecification {
    
    private final Optional<String> usernameContains;
    private final Optional<String> emailContains;
    private final Optional<String> firstNameContains;
    private final Optional<String> lastNameContains;
    private final Optional<List<Role>> roles;
    
    private UserSpecification(Builder builder) {
        this.usernameContains = Optional.ofNullable(builder.usernameContains);
        this.emailContains = Optional.ofNullable(builder.emailContains);
        this.firstNameContains = Optional.ofNullable(builder.firstNameContains);
        this.lastNameContains = Optional.ofNullable(builder.lastNameContains);
        this.roles = Optional.ofNullable(builder.roles);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Optional<String> getUsernameContains() {
        return usernameContains;
    }
    
    public Optional<String> getEmailContains() {
        return emailContains;
    }
    
    public Optional<String> getFirstNameContains() {
        return firstNameContains;
    }
    
    public Optional<String> getLastNameContains() {
        return lastNameContains;
    }
    
    public Optional<List<Role>> getRoles() {
        return roles;
    }
    
    public static class Builder {
        private String usernameContains;
        private String emailContains;
        private String firstNameContains;
        private String lastNameContains;
        private List<Role> roles;
        
        public Builder usernameContains(String username) {
            this.usernameContains = username;
            return this;
        }
        
        public Builder emailContains(String email) {
            this.emailContains = email;
            return this;
        }
        
        public Builder firstNameContains(String firstName) {
            this.firstNameContains = firstName;
            return this;
        }
        
        public Builder lastNameContains(String lastName) {
            this.lastNameContains = lastName;
            return this;
        }
        
        public Builder withRoles(List<Role> roles) {
            this.roles = roles;
            return this;
        }
        
        public UserSpecification build() {
            return new UserSpecification(this);
        }
    }
}