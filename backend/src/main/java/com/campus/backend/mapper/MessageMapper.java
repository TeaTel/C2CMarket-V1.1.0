package com.campus.backend.mapper;

import com.campus.backend.entity.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper {
    
    @Insert("INSERT INTO messages (sender_id, receiver_id, content, product_id, message_type, is_read) " +
            "VALUES (#{senderId}, #{receiverId}, #{content}, #{productId}, #{messageType}, FALSE)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);
    
    @Select("SELECT * FROM messages WHERE id = #{id}")
    Message selectById(Integer id);
    
    @Select("SELECT * FROM messages WHERE (sender_id = #{userId} AND receiver_id = #{otherUserId}) " +
            "OR (sender_id = #{otherUserId} AND receiver_id = #{userId}) " +
            "ORDER BY created_at DESC")
    List<Message> selectConversation(@Param("userId") Integer userId, @Param("otherUserId") Integer otherUserId);
    
    @Select("SELECT DISTINCT CASE " +
            "WHEN sender_id = #{userId} THEN receiver_id " +
            "WHEN receiver_id = #{userId} THEN sender_id " +
            "END as contact_id " +
            "FROM messages " +
            "WHERE sender_id = #{userId} OR receiver_id = #{userId} " +
            "ORDER BY MAX(created_at) DESC")
    List<Integer> selectContactIds(Integer userId);
    
    @Select("SELECT * FROM messages WHERE receiver_id = #{userId} AND is_read = FALSE")
    List<Message> selectUnreadMessages(Integer userId);
    
    @Update("UPDATE messages SET is_read = TRUE WHERE id = #{id}")
    int markAsRead(Integer id);
    
    @Update("UPDATE messages SET is_read = TRUE WHERE receiver_id = #{userId} AND sender_id = #{senderId}")
    int markConversationAsRead(@Param("userId") Integer userId, @Param("senderId") Integer senderId);
}