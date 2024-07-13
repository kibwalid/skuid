package com.onara.backend.modules.user_module.repositories;

import com.onara.backend.modules.user_module.models.entities.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserInfo, Integer> {

    UserInfo findByUsername(String username);
}
