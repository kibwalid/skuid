package com.onara.backend.modules.user_module.models.dto;

import com.onara.backend.modules.user_module.models.entities.Role;
import com.onara.backend.modules.user_module.models.entities.UserInfo;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String name;
    private String username;
    private String password;
    private List<String> roles;

    public UserInfoDTO(UserInfo userInfo) {
        this.name = userInfo.getName();
        this.username = userInfo.getUsername();
        this.password = userInfo.getPassword();
        this.roles = userInfo.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList());
    }
}
