package com.fdsc.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.message.entity.Conversation;
import com.fdsc.module.message.entity.Message;
import com.fdsc.module.message.mapper.ConversationMapper;
import com.fdsc.module.message.mapper.MessageMapper;
import com.fdsc.module.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    public Conversation createConversation(Long customerId, Long companyId, Long propertyId) {
        Conversation conv = new Conversation();
        conv.setCustomerId(customerId);
        conv.setCompanyId(companyId);
        conv.setPropertyId(propertyId);
        conv.setStatus("OPEN");
        conv.setCustomerUnread(0);
        conv.setAgentUnread(0);
        conversationMapper.insert(conv);
        return conv;
    }

    @Override
    public void sendMessage(Long conversationId, Long senderId, String senderRole, String content) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) throw new BusinessException(404, "会话不存在");

        Message msg = new Message();
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setSenderRole(senderRole);
        msg.setContent(content);
        messageMapper.insert(msg);

        if ("CUSTOMER".equals(senderRole)) {
            conv.setAgentUnread(conv.getAgentUnread() + 1);
        } else {
            conv.setCustomerUnread(conv.getCustomerUnread() + 1);
        }
        conversationMapper.updateById(conv);
    }

    @Override
    public List<Message> getMessages(Long conversationId, int page, int size) {
        Page<Message> p = messageMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<Message>().eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt));
        return p.getRecords();
    }

    @Override
    public List<Conversation> listConversations(Long userId, String role) {
        LambdaQueryWrapper<Conversation> qw = new LambdaQueryWrapper<>();
        if ("CUSTOMER".equals(role)) qw.eq(Conversation::getCustomerId, userId);
        else qw.eq(Conversation::getCompanyId, userId);
        return conversationMapper.selectList(qw.orderByDesc(Conversation::getUpdatedAt));
    }
}
