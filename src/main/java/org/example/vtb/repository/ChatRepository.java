package org.example.vtb.repository;

import org.example.vtb.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository extends JpaRepository<Chat, UUID> {
    Optional<Chat> findByUserId(UUID userId);
    
    @Query("SELECT c FROM Chat c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<Chat> findAllActiveChats();
    
    @Query("SELECT c FROM Chat c WHERE c.user.id = :userId AND c.isActive = true")
    Optional<Chat> findActiveChatByUserId(UUID userId);
} 