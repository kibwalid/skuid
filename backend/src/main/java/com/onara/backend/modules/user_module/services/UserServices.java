package com.onara.backend.modules.user_module.services;


import com.onara.backend.config.exceptions.AppException;
import com.onara.backend.models.AppResponse;
import com.onara.backend.models.MessageResponse;
import com.onara.backend.modules.user_module.models.dto.AuthRequest;
import com.onara.backend.modules.user_module.models.dto.AuthResponse;
import com.onara.backend.modules.user_module.models.dto.ChangePasswordRequest;
import com.onara.backend.modules.user_module.models.dto.UserInfoDTO;
import com.onara.backend.modules.user_module.models.entities.Role;
import com.onara.backend.modules.user_module.models.entities.UserInfo;
import com.onara.backend.modules.user_module.repositories.UserRepository;
import com.onara.backend.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtil;


    public UserServices(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
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


    public MessageResponse disableAccount(String token) {
        UserInfo userInfo = getUserInfo(token);
        userInfo.setActive(false);
        userRepository.save(userInfo);
        return new MessageResponse("User ID has been deactivated");
    }

    private void throwExceptionIfUserInactive(UserInfo userInfo) {
        if(!userInfo.isActive()) {
            throw new AppException("User ID is deactivated. You cannot edit its information", HttpStatus.UNAUTHORIZED);
        }
    }

    public UserInfoDTO updateUser(String token, UserInfoDTO updatedUserDTO) {
        UserInfo userInfo = getUserInfo(token);
        throwExceptionIfUserInactive(userInfo);

        userInfo.setName(updatedUserDTO.getName());
        userInfo.setUsername(updatedUserDTO.getUsername());

        userRepository.save(userInfo);
        return new UserInfoDTO(userInfo);
    }

    public MessageResponse changePassword(String token, ChangePasswordRequest changePasswordRequest) {
        UserInfo userInfo = getUserInfo(token);
        throwExceptionIfUserInactive(userInfo);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userInfo.getUsername(), changePasswordRequest.getOldPassword())
        );

        String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        userInfo.setPassword(encodedPassword);

        return new MessageResponse("Password has been changed successfully");
    }

    public String login(AuthRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
        );
        final UserDetails userDetails = loadUserByUsername(authenticationRequest.getUsername());
        return jwtUtil.generateToken(userDetails);
    }
}
