package org.example.vtb.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MessageRequest {
    private UUID productId;
    private UUID receiverId;
    private String content;
}