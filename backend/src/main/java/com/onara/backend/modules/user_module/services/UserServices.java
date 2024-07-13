package com.onara.backend.modules.user_module.services;


import com.onara.backend.config.exceptions.AppException;
import com.onara.backend.modules.user_module.models.dto.AuthResponse;
import com.onara.backend.modules.user_module.models.dto.RegisterRequest;
import com.onara.backend.modules.user_module.models.entities.Role;
import com.onara.backend.modules.user_module.models.entities.UserInfo;
import com.onara.backend.modules.user_module.repositories.UserRepository;
import com.onara.backend.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServices implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtil;


    public UserServices(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userRepository.findByUsername(username);
        if (userInfo == null) {
            throw new UsernameNotFoundException("User with username " + username + " does not exist in our database");
        }
        return new User(userInfo.getUsername(), userInfo.getPassword(), userInfo.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority())).collect(Collectors.toList()));
    }

    public AuthResponse registerUser(RegisterRequest newUser) throws AppException {

        if (userRepository.findByUsername(newUser.getUsername()) != null) {
            throw new AppException("User with given username already exists", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(newUser.getPassword());
        List<Role> roles = newUser.getRoles().stream()
                .map(role -> Role.valueOf(role.toUpperCase()))
                .toList();

        UserInfo user = new UserInfo();
        user.setName(newUser.getName());
        user.setUsername(newUser.getUsername());
        user.setPassword(encodedPassword);
        user.setRoles(roles);

        userRepository.save(user);

        String token = jwtUtil.generateToken(new User(
                user.getUsername(), user.getPassword(), user.getRoles()));

        return new AuthResponse(token);
    }

}
