package com.leave.management.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leave.management.system.enums.UserPermission;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String profile;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPermission permissions; // ADMIN, STAFF, or MANAGER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team; // Optional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department; // Optional

    @Transient
    private List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

    @JsonIgnore
    private boolean accountLocked = false;

    @JsonIgnore
    private boolean credentialsExpired = false;

    private boolean accountEnabled = true;

    public void addAuthority(String authority) {
        if (authorityList == null)
            authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(authority));
    }

    public List<SimpleGrantedAuthority> getAuthorityList() {
        return List.of(new SimpleGrantedAuthority(permissions.name()));
    }

    public String getAuthorities() {
        return permissions.name();
    }
}
