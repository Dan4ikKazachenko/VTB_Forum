package org.example.vtb.controller;

import lombok.RequiredArgsConstructor;
import org.example.vtb.dto.MessageRequest;
import org.example.vtb.entity.Message;
import org.example.vtb.entity.User;
import org.example.vtb.entity.Role;
import org.example.vtb.repository.MessageRepository;
import org.example.vtb.repository.ProductRepository;
import org.example.vtb.repository.UserRepository;
import org.example.vtb.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Message>> getMessagesByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(messageRepository.findByProductIdOrderByCreatedAtAsc(productId));
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        User receiver;
        if (userDetails.getUser().getRole() == Role.ADMIN) {
            if (request.getReceiverId() == null) {
                throw new RuntimeException("receiverId обязателен для админа");
            }
            receiver = userRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        } else {
            receiver = product.getCreatedBy();
        }

        var message = Message.builder()
                .content(request.getContent())
                .product(product)
                .sender(userDetails.getUser())
                .receiver(receiver)
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(messageRepository.save(message));
    }

    @GetMapping("/product/{productId}/my")
    public ResponseEntity<List<Message>> getMessagesForUser(
            @PathVariable UUID productId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID currentUserId = userDetails.getUser().getId();
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));
        UUID adminId = product.getCreatedBy().getId();

        var messages = messageRepository.findByProductIdAndSenderIdOrReceiverIdOrderByCreatedAtAsc(
                productId, currentUserId, adminId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/product/{productId}/with/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Message>> getChatForAdmin(
            @PathVariable UUID productId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetailsImpl adminDetails) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Продукт не найден"));

        UUID adminId = adminDetails.getUser().getId();

        if (!product.getCreatedBy().getId().equals(adminId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var messages = messageRepository.findByProductIdAndSenderIdOrReceiverIdOrderByCreatedAtAsc(
                productId, adminId, userId);
        return ResponseEntity.ok(messages);
    }
}
