package org.example.vtb.repository;

import org.example.vtb.entity.Message;
import org.example.vtb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

        List<Message> findByProductIdOrderByCreatedAtAsc(UUID productId);

        @Query("SELECT m FROM Message m WHERE m.product.id = :productId " +
                        "AND (m.sender.id = :userId1 OR m.receiver.id = :userId1) " +
                        "AND (m.sender.id = :userId2 OR m.receiver.id = :userId2) " +
                        "ORDER BY m.createdAt ASC")
        List<Message> findByProductIdAndSenderIdOrReceiverIdOrderByCreatedAtAsc(
                        @Param("productId") UUID productId,
                        @Param("userId1") UUID userId1,
                        @Param("userId2") UUID userId2);

        @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.product.id = :productId")
        List<User> findDistinctUsersByProduct(@Param("productId") UUID productId);

        List<Message> findByChatIdOrderByCreatedAtAsc(UUID chatId);

        @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.chat.id = :chatId ORDER BY m.createdAt ASC")
        List<Message> findByChatIdWithSenderOrderByCreatedAtAsc(@Param("chatId") UUID chatId);

        @Modifying
        @Query("UPDATE Message m SET m.isRead = true WHERE m.chat.id = :chatId")
        void updateMessagesAsRead(@Param("chatId") UUID chatId);

        long countByChatIdAndIsReadFalse(UUID chatId);
}