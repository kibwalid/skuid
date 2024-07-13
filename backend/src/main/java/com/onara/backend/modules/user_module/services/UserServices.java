package com.onara.backend.modules.user_module.services;


import com.onara.backend.config.exceptions.AppException;
import com.onara.backend.models.AppResponse;
import com.onara.backend.models.MessageResponse;
import com.onara.backend.modules.user_module.models.dto.AuthResponse;
import com.onara.backend.modules.user_module.models.dto.UserInfoDTO;
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

    public AuthResponse registerUser(UserInfoDTO newUser) throws AppException {

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
        user.setActive(true);

        userRepository.save(user);

        String token = jwtUtil.generateToken(new User(
                user.getUsername(), user.getPassword(), user.getRoles()));

        return new AuthResponse(token);
    }

    public UserInfo getUserInfo(String token) {
        String jwt = token.substring(7);
        String username = jwtUtil.extractUsername(jwt);
        UserInfo userInfo = userRepository.findByUsername(username);
        if(userInfo == null) {
            throw new AppException("User with given username does not exist in our database", HttpStatus.NOT_FOUND);
        }
        return userInfo;
    }


    public AppResponse disableAccount(String token) {
        UserInfo userInfo = getUserInfo(token);
        userInfo.setActive(false);
        userRepository.save(userInfo);
        return new AppResponse(new MessageResponse("User ID has been deactivated"),true);
    }

    public UserInfoDTO updateUser(String token, UserInfoDTO updatedUserDTO) {
        UserInfo userInfo = getUserInfo(token);
        if(!userInfo.isActive()) {
            throw new AppException("User ID is deactivated. You cannot edit its information", HttpStatus.UNAUTHORIZED);
        }
        userInfo.setName(updatedUserDTO.getName());
        userInfo.setUsername(updatedUserDTO.getUsername());

        userRepository.save(userInfo);
        return new UserInfoDTO(userInfo);
    }
}
