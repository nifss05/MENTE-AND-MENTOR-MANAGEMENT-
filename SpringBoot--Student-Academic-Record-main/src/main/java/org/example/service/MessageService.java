package org.example.service;

import org.example.model.Message;
import org.example.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public void sendMessage(String mentorId, String studentUsn, String messageText) {

        Message message = new Message();
        message.setMentorId(mentorId);
        message.setStudentUsn(studentUsn);
        message.setMessage(messageText);
        message.setRead(false);

        messageRepository.save(message);
    }

    public List<Message> getStudentMessages(String studentUsn) {
        return messageRepository.findByStudentUsnOrderByCreatedAtDesc(studentUsn);
    }

    public void markAsRead(Long id) {

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setRead(true);

        messageRepository.save(message);
    }
}