package com.campus.backend.service;

import com.campus.backend.dto.MessageSendDTO;
import com.campus.backend.dto.MessageVO;
import java.util.List;

public interface MessageService {
    
    MessageVO sendMessage(MessageSendDTO sendDTO, Long senderId);
    
    List<MessageVO> getConversation(Long userId, Long otherUserId);
    
    List<MessageVO> getContacts(Long userId);
    
    List<MessageVO> getUnreadMessages(Long userId);
    
    void markAsRead(Long messageId, Long userId);
    
    void markConversationAsRead(Long userId, Long senderId);
    
    int getUnreadCount(Long userId);
}