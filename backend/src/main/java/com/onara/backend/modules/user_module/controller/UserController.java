package com.onara.backend.modules.user_module.controller;

import com.onara.backend.models.AppResponse;
import com.onara.backend.modules.user_module.models.dto.AuthRequest;
import com.onara.backend.modules.user_module.models.dto.AuthResponse;
import com.onara.backend.modules.user_module.models.dto.ChangePasswordRequest;
import com.onara.backend.modules.user_module.models.dto.UserInfoDTO;
import com.onara.backend.modules.user_module.models.entities.UserInfo;
import com.onara.backend.modules.user_module.services.UserServices;
import com.onara.backend.utils.JwtUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserServices userServices;

    public UserController(UserServices userServices, JwtUtils jwtUtil) {
        this.userServices = userServices;
    }

    @PostMapping("/login")
    public AppResponse login(@RequestBody AuthRequest authenticationRequest) throws Exception {
        return new AppResponse(new AuthResponse(userServices.login(authenticationRequest)), true);
    }

    @PostMapping("/register")
    public AppResponse register(@RequestBody UserInfoDTO newUser) throws Exception {
        return new AppResponse(userServices.registerUser(newUser), true);
    }


    @GetMapping("/")
    public AppResponse getUserInfo(@RequestHeader("Authorization") String token) {

        return new AppResponse(userServices.getUserInfo(token), true);
    }

    @PutMapping("/")
    public AppResponse disableAccount(@RequestHeader("Authorization") String token) {
        return new AppResponse(userServices.disableAccount(token), true);
    }

    @PutMapping("/")
    public AppResponse updateUser(@RequestHeader("Authorization") String token, @RequestBody UserInfoDTO updatedUserDTO) {
        return new AppResponse(userServices.updateUser(token, updatedUserDTO), true);
    }

    @PutMapping("/")
    public AppResponse changePassword(@RequestHeader("Authorization") String token, @RequestBody ChangePasswordRequest changePasswordRequest) {
        return new AppResponse(userServices.changePassword(token, changePasswordRequest), true);
    }

}
