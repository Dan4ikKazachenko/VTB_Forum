package org.example.vtb.controller;

import lombok.RequiredArgsConstructor;
import org.example.vtb.dto.ChatDTO;
import org.example.vtb.dto.MessageRequest;
import org.example.vtb.dto.UserDto;
import org.example.vtb.entity.Chat;
import org.example.vtb.entity.Message;
import org.example.vtb.entity.Role;
import org.example.vtb.entity.User;
import org.example.vtb.repository.ChatRepository;
import org.example.vtb.repository.MessageRepository;
import org.example.vtb.repository.UserRepository;
import org.example.vtb.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<Chat> getOrCreateMyChat(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Chat chat = chatRepository.findActiveChatByUserId(user.getId())
                .orElseGet(() -> chatRepository.save(Chat.builder()
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .isActive(true)
                        .build()));
        return ResponseEntity.ok(chat);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChatDTO>> getAllActiveChats() {
        List<ChatDTO> chatDTOs = chatRepository.findAllActiveChats().stream().map(chat -> {
            ChatDTO dto = new ChatDTO();
            dto.setId(chat.getId());
            dto.setCreatedAt(chat.getCreatedAt());
            dto.setLastMessage(chat.getLastMessage());
            dto.setActive(chat.isActive());

            User user = chat.getUser();
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setEmail(user.getEmail());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setRole(user.getRole());

            dto.setUser(userDto);

            return dto;
        }).toList();

        return ResponseEntity.ok(chatDTOs);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getChatMessages(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));

        if (!isAuthorized(userDetails, chat)) {
            return ResponseEntity.status(403).build();
        }

        List<Message> messages = messageRepository.findByChatIdWithSenderOrderByCreatedAtAsc(chatId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable UUID chatId,
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));

        if (!isAuthorized(userDetails, chat)) {
            return ResponseEntity.status(403).build();
        }

        Message message = Message.builder()
                .content(request.getContent())
                .chat(chat)
                .sender(userDetails.getUser())
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        chat.setLastMessage(request.getContent());
        chatRepository.save(chat);

        return ResponseEntity.ok(messageRepository.save(message));
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));

        if (!isAuthorized(userDetails, chat)) {
            return ResponseEntity.status(403).build();
        }

        messageRepository.updateMessagesAsRead(chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{chatId}/unread")
    public ResponseEntity<Long> getUnreadMessagesCount(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));

        if (!isAuthorized(userDetails, chat)) {
            return ResponseEntity.status(403).build();
        }

        long count = messageRepository.countByChatIdAndIsReadFalse(chatId);
        return ResponseEntity.ok(count);
    }

    private boolean isAuthorized(UserDetailsImpl userDetails, Chat chat) {
        return userDetails.getUser().getRole() == Role.ADMIN ||
                chat.getUser().getId().equals(userDetails.getUser().getId());
    }
}
