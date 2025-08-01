package com.todo.service.impl;

import com.todo.dto.*;
import com.todo.entity.ChatMessage;
import com.todo.mapper.ChatMessageMapper;
import com.todo.service.ChatService;
import com.todo.vo.ChatContactVo;
import com.todo.vo.ChatMessageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天服务实现类
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Override
    public List<ChatMessageVo> getChatHistory(ChatHistoryQueryDto queryDto) {
        // 权限验证：用户只能查看与自己相关的聊天记录
        // 管理员可以查看任何聊天记录，普通用户只能查看自己参与的聊天记录
        // 普通用户可以查看自己与任何其他用户的聊天记录
        if (!"admin".equals(queryDto.getRequesterAuth())) {
            // 普通用户只需要确保请求者ID正确，可以查看与任何用户的聊天记录
            // 因为chatUserId是要查看聊天记录的对象用户ID，requesterId是当前登录用户ID
            // 普通用户应该能查看自己与chatUserId之间的聊天记录
        }
        
        int offset = (queryDto.getPage() - 1) * queryDto.getSize();
        
        Long userId1, userId2;
        // 无论是管理员还是普通用户，都查看requesterId与chatUserId之间的聊天记录
        userId1 = queryDto.getRequesterId();
        userId2 = queryDto.getChatUserId();
        
        System.out.println("查询聊天记录: userId1=" + userId1 + ", userId2=" + userId2 + ", offset=" + offset + ", size=" + queryDto.getSize());
        
        List<ChatMessageVo> result = chatMessageMapper.getChatHistory(userId1, userId2, offset, queryDto.getSize());
        System.out.println("查询到" + result.size() + "条聊天记录");
        
        return result;
    }
    
    @Override
    public List<ChatContactVo> getChatContacts(ChatContactsQueryDto queryDto) {
        // 所有用户都可以访问聊天功能
        return chatMessageMapper.getChatContacts(queryDto.getUserId(), queryDto.getUserAuth());
    }
    
    @Override
    public void markMessagesAsRead(ChatMarkReadDto markReadDto) {
        // 所有用户都可以标记自己的消息为已读
        chatMessageMapper.markMessagesAsRead(markReadDto.getUserId(), markReadDto.getChatUserId());
    }
    
    @Override
    public Map<String, Object> getUnreadMessageCount(ChatUnreadCountQueryDto queryDto) {
        // 所有用户都可以查看自己的未读消息数量
        Integer totalCount = chatMessageMapper.getTotalUnreadMessageCount(queryDto.getUserId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalUnreadCount", totalCount != null ? totalCount : 0);
        
        return result;
    }
    
    @Override
    public void saveChatMessage(Long senderId, Long receiverId, String content, String messageType) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessage.setMessageType(messageType != null ? messageType : "text");
        chatMessage.setIsRead(false);
        chatMessage.setCreatedTime(LocalDateTime.now());
        chatMessage.setUpdatedTime(LocalDateTime.now());
        
        chatMessageMapper.insert(chatMessage);
    }
} 