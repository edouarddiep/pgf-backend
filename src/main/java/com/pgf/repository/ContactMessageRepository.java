package com.pgf.repository;

import com.pgf.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderByCreatedAtDesc();
    List<ContactMessage> findByIsReadFalseOrderByCreatedAtDesc();
    Long countByIsReadFalse();
    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactMessage.MessageStatus status);
}
