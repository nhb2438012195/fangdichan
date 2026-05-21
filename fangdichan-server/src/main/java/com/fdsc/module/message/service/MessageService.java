package com.fdsc.module.message.service;

import com.fdsc.module.message.entity.Conversation;
import com.fdsc.module.message.entity.Message;

import java.util.List;

public interface MessageService {
    Conversation createConversation(Long customerId, Long companyId, Long propertyId);
    void sendMessage(Long conversationId, Long senderId, String senderRole, String content);
    List<Message> getMessages(Long conversationId, int page, int size);
    List<Conversation> listConversations(Long userId, String role);
}
