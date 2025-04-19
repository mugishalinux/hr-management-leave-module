package com.leave.management.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "user")

public class User {
    @Id
    public String id = UUID.randomUUID().toString();
    protected String username;
    @JsonIgnore
    protected String password;
    @Transient
    protected List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
    protected String permissions;

    @JsonIgnore
    protected boolean firstAuth = true;
    @JsonIgnore
    protected boolean accountExpired = false;
    @JsonIgnore
    protected boolean accountLocked = false;
    @JsonIgnore
    protected boolean credentialsExpired = false;
    protected boolean accountEnabled = true;

    @JsonIgnore
    protected int risk;

//    @PrePersist
//    protected void runBeforeSave(){
//        this.permissions =  String.join(", ", this.authorityList.stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()));
//    }


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void addAuthority(String authority) {
        if (authorityList == null)
            authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(authority));
        permissions = String.join(",", this.authorityList.stream().map(auth -> auth.getAuthority()).collect(Collectors.toList()));
    }
    public List<SimpleGrantedAuthority> getAuthorityList() {
        if (permissions == null || permissions.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(permissions.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    public String getAuthorities() {
        return permissions;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authorityList=" + authorityList +
                ", permissions='" + permissions + '\'' +
                ", firstAuth=" + firstAuth +
                ", accountExpired=" + accountExpired +
                ", accountLocked=" + accountLocked +
                ", credentialsExpired=" + credentialsExpired +
                ", accountEnabled=" + accountEnabled +
                ", risk=" + risk +
                '}';
    }
}
