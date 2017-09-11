package com.techolution.customerdb.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Customer {

    private static final Logger LOG = Logger.getLogger(Customer.class);

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false, updatable = false)
    @NotEmpty(message = "Email should not be empty")
    @NotNull(message = "Email should not be null")
    @Email
    private String email;
    @Column(unique = true, nullable = false, updatable = false)
    @NotEmpty(message = "Username should not be empty")
    @NotNull(message = "Username should not be null")
    private String username;

    @JsonIgnore
    private String password;

    public boolean matchesPassword(String password) {
        return this.password.equals(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public String toString() {
        try {
            return toJson();
        } catch (JsonProcessingException e) {
            LOG.error("Json parse error : ", e);
        }
        return super.toString();
    }

    public String getPassword() {
        return password;
    }
}
