package com.onara.backend.modules.user_module.controller;

import com.onara.backend.modules.user_module.models.dto.AuthRequest;
import com.onara.backend.modules.user_module.models.dto.AuthResponse;
import com.onara.backend.modules.user_module.models.dto.RegisterRequest;
import com.onara.backend.modules.user_module.services.UserServices;
import com.onara.backend.utils.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {

    private final UserServices userServices;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtil;

    public UserController(UserServices userServices, AuthenticationManager authenticationManager, JwtUtils jwtUtil) {
        this.userServices = userServices;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest authenticationRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );
        final UserDetails userDetails = userServices.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);
        return new AuthResponse(jwt);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest newUser) throws Exception {
        return userServices.registerUser(newUser);
    }


}
