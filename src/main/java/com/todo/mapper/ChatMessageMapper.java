package com.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.todo.entity.ChatMessage;
import com.todo.vo.ChatContactVo;
import com.todo.vo.ChatMessageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 获取两个用户之间的聊天记录
     */
    List<ChatMessageVo> getChatHistory(@Param("userId1") Long userId1, 
                                      @Param("userId2") Long userId2, 
                                      @Param("offset") Integer offset, 
                                      @Param("size") Integer size);
    
    /**
     * 获取用户的聊天联系人列表
     */
    List<ChatContactVo> getChatContacts(@Param("userId") Long userId, @Param("userAuth") String userAuth);
    
    /**
     * 标记消息为已读
     */
    void markMessagesAsRead(@Param("userId") Long userId, @Param("chatUserId") Long chatUserId);
    
    /**
     * 获取用户未读消息数量
     */
    Integer getUnreadMessageCount(@Param("userId") Long userId, @Param("chatUserId") Long chatUserId);
    
    /**
     * 获取用户总未读消息数量
     */
    Integer getTotalUnreadMessageCount(@Param("userId") Long userId);
} 