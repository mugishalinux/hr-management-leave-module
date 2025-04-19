package com.leave.management.system.model;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleUserAuth {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

    public boolean isEmpty() {
        return (this.username == null || this.username.isEmpty()) &&
                (this.password == null || this.password.isEmpty());
    }

}
