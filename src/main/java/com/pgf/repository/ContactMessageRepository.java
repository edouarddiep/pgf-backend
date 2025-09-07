package com.pgf.repository;

import com.pgf.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.MessageStatus status);

    List<ContactMessage> findByIsReadFalseOrderByCreatedAtDesc();

    @Query("SELECT COUNT(cm) FROM ContactMessage cm WHERE cm.isRead = false")
    long countUnreadMessages();

    List<ContactMessage> findAllByOrderByCreatedAtDesc();
}