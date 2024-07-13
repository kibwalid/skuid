package com.onara.backend.modules.user_module.models.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String name;
    private String username;
    private String password;
    private List<String> roles;
}
