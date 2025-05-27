package org.example.vtb.controller;

import lombok.RequiredArgsConstructor;
import org.example.vtb.dto.UserDto;
import org.example.vtb.entity.User;
import org.example.vtb.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal org.example.vtb.security.UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @AuthenticationPrincipal org.example.vtb.security.UserDetailsImpl userDetails,
            @RequestBody UserDto userDto) {

        User user = userDetails.getUser();

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }


        User updatedUser = userRepository.save(user);

        UserDto updatedUserDto = UserDto.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .role(updatedUser.getRole())
                .build();

        return ResponseEntity.ok(updatedUserDto);
    }
}