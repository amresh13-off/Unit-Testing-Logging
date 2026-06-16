package com.example.task3.dto;

import com.example.task3.entity.UserRole;

public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private Boolean active;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String firstName, String lastName, String email, UserRole role, Boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private UserRole role;
        private Boolean active;

        Builder() {}

        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }
        public Builder active(Boolean active) { this.active = active; return this; }

        public UpdateUserRequest build() {
            return new UpdateUserRequest(firstName, lastName, email, role, active);
        }
    }
}
