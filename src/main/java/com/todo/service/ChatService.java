package com.todo.service;

import com.todo.dto.*;
import com.todo.vo.ChatContactVo;
import com.todo.vo.ChatMessageVo;

import java.util.List;
import java.util.Map;

/**
 * 聊天服务接口
 */
public interface ChatService {
    
    /**
     * 获取聊天记录
     */
    List<ChatMessageVo> getChatHistory(ChatHistoryQueryDto queryDto);
    
    /**
     * 获取聊天联系人列表
     */
    List<ChatContactVo> getChatContacts(ChatContactsQueryDto queryDto);
    
    /**
     * 标记消息为已读
     */
    void markMessagesAsRead(ChatMarkReadDto markReadDto);
    
    /**
     * 获取未读消息数量
     */
    Map<String, Object> getUnreadMessageCount(ChatUnreadCountQueryDto queryDto);
    
    /**
     * 保存聊天消息
     */
    void saveChatMessage(Long senderId, Long receiverId, String content, String messageType);
} 