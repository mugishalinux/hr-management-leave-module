package com.leave.management.system.security;


import com.leave.management.system.model.User;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Data
public class AuthenticatedUser {
    private String id;
    private String email;
    protected List<SimpleGrantedAuthority> authorityList;

    public AuthenticatedUser() {
    }

    public AuthenticatedUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.authorityList = user.getAuthorityList();
    }

    public AuthenticatedUser(String id, String username, List<SimpleGrantedAuthority> authorityList) {
        this.id = id;
        this.email = username;
        this.authorityList = authorityList;
    }

    public boolean hasAuthority(String auth) {
        for (int i = 0; i < authorityList.size(); i++)
            if (authorityList.get(i).getAuthority().equals(auth))
                return true;
        return false;
    }

    @Override
    public String toString() {
        return "UserSimpleDetails{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", authorityList=" + authorityList +
                '}';
    }
}
