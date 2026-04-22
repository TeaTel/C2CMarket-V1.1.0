package com.campus.backend.service.impl;

import com.campus.backend.dto.MessageSendDTO;
import com.campus.backend.dto.MessageVO;
import com.campus.backend.dto.ProductVO;
import com.campus.backend.dto.UserVO;
import com.campus.backend.entity.Message;
import com.campus.backend.mapper.MessageMapper;
import com.campus.backend.mapper.ProductMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.MessageService;
import com.campus.backend.service.ProductService;
import com.campus.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public MessageVO sendMessage(MessageSendDTO sendDTO, Long senderId) {
        // 检查接收者是否存在
        if (userMapper.selectById(sendDTO.getReceiverId()) == null) {
            throw new RuntimeException("接收者不存在");
        }
        
        // 检查商品是否存在（如果提供了商品ID）
        if (sendDTO.getProductId() != null && productMapper.selectById(sendDTO.getProductId()) == null) {
            throw new RuntimeException("商品不存在");
        }
        
        Message message = new Message();
        BeanUtils.copyProperties(sendDTO, message);
        message.setSenderId(senderId);
        message.setCreatedAt(LocalDateTime.now());
        
        messageMapper.insert(message);
        return convertToVO(message);
    }

    @Override
    public List<MessageVO> getConversation(Long userId, Long otherUserId) {
        List<Message> messages = messageMapper.selectConversation(userId.intValue(), otherUserId.intValue());
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<MessageVO> getContacts(Long userId) {
        List<Integer> contactIds = messageMapper.selectContactIds(userId.intValue());
        List<MessageVO> contacts = new ArrayList<>();
        
        for (Integer contactId : contactIds) {
            // 获取最近一条消息
            List<Message> messages = messageMapper.selectConversation(userId.intValue(), contactId.intValue());
            if (!messages.isEmpty()) {
                MessageVO messageVO = convertToVO(messages.get(0));
                contacts.add(messageVO);
            }
        }
        
        return contacts;
    }

    @Override
    public List<MessageVO> getUnreadMessages(Long userId) {
        List<Message> messages = messageMapper.selectUnreadMessages(userId.intValue());
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId.intValue());
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        
        // 检查权限：只有接收者可以标记为已读
        if (!message.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权标记此消息为已读");
        }
        
        messageMapper.markAsRead(messageId.intValue());
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long userId, Long senderId) {
        messageMapper.markConversationAsRead(userId.intValue(), senderId.intValue());
    }

    @Override
    public int getUnreadCount(Long userId) {
        List<Message> messages = messageMapper.selectUnreadMessages(userId.intValue());
        return messages.size();
    }
    
    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);

        // 获取发送者信息
        try {
            if (message.getSenderId() != null) {
                UserVO senderInfo = userService.getUserInfo(message.getSenderId().longValue());
                vo.setSenderInfo(senderInfo);
            }
        } catch (Exception e) {
            logger.warn("获取发送者信息失败, senderId={}: {}", message.getSenderId(), e.getMessage());
        }

        // 获取接收者信息
        try {
            if (message.getReceiverId() != null) {
                UserVO receiverInfo = userService.getUserInfo(message.getReceiverId().longValue());
                vo.setReceiverInfo(receiverInfo);
            }
        } catch (Exception e) {
            logger.warn("获取接收者信息失败, receiverId={}: {}", message.getReceiverId(), e.getMessage());
        }

        // 获取商品信息
        try {
            if (message.getProductId() != null) {
                ProductVO productInfo = productService.getProductDetail(message.getProductId().longValue());
                vo.setProductInfo(productInfo);
            }
        } catch (Exception e) {
            logger.warn("获取商品信息失败, productId={}: {}", message.getProductId(), e.getMessage());
        }

        return vo;
    }
}