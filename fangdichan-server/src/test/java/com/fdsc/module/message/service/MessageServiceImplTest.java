package com.fdsc.module.message.service;

import com.fdsc.module.message.entity.Conversation;
import com.fdsc.module.message.mapper.ConversationMapper;
import com.fdsc.module.message.mapper.MessageMapper;
import com.fdsc.module.message.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {
    @Mock private ConversationMapper conversationMapper;
    @Mock private MessageMapper messageMapper;
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() { messageService = new MessageServiceImpl(conversationMapper, messageMapper); }

    @Test
    void createConversation_shouldInsert() {
        messageService.createConversation(1L, 1L, 1L);
        verify(conversationMapper).insert(any());
    }

    @Test
    void sendMessage_shouldInsertAndUpdateUnread() {
        Conversation conv = new Conversation();
        conv.setId(1L);
        conv.setCustomerUnread(0);
        conv.setAgentUnread(0);
        when(conversationMapper.selectById(1L)).thenReturn(conv);

        messageService.sendMessage(1L, 1L, "CUSTOMER", "你好");
        verify(messageMapper).insert(any());
        verify(conversationMapper).updateById(any());
    }

    @Test
    void getMessages_shouldReturnList() {
        when(messageMapper.selectPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        assertNotNull(messageService.getMessages(1L, 1, 20));
    }

    @Test
    void listConversations_shouldReturnForCustomer() {
        when(conversationMapper.selectList(any())).thenReturn(List.of(new Conversation()));
        assertEquals(1, messageService.listConversations(1L, "CUSTOMER").size());
    }
}
