package org.example.vtb.service;

import org.example.vtb.dto.LoginRequest;
import org.example.vtb.dto.RegisterRequest;
import org.example.vtb.dto.UserDto;
import org.example.vtb.entity.Role;
import org.example.vtb.entity.User;
import org.example.vtb.repository.UserRepository;
import org.example.vtb.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public record AuthResponse(String token, UserDto user) {
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();

        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authManager.authenticate(authToken);

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();

        return new AuthResponse(token, userDto);
    }

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );
    }
}