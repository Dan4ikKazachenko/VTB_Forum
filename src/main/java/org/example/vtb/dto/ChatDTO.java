package org.example.vtb.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ChatDTO {
    private UUID id;
    private UserDto user;
    private LocalDateTime createdAt;
    private String lastMessage;
    private boolean active;
}
