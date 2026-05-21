package com.fdsc.module.message.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.message.entity.Conversation;
import com.fdsc.module.message.entity.Message;
import com.fdsc.module.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/message")
@RequiredArgsConstructor
public class CustomerMessageController {
    private final MessageService messageService;

    @PostMapping("/conversation")
    public Result<Conversation> createConversation(@AuthenticationPrincipal Long userId,
                                                    @RequestParam Long companyId,
                                                    @RequestParam(required = false) Long propertyId) {
        return Result.success(messageService.createConversation(userId, companyId, propertyId));
    }

    @GetMapping("/conversations")
    public Result<List<Conversation>> listConversations(@AuthenticationPrincipal Long userId) {
        return Result.success(messageService.listConversations(userId, "CUSTOMER"));
    }

    @GetMapping("/{conversationId}")
    public Result<List<Message>> getMessages(@PathVariable Long conversationId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.getMessages(conversationId, page, size));
    }
}
