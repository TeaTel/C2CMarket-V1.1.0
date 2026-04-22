package com.campus.backend.mapper;

import com.campus.backend.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    
    @Insert("INSERT INTO users (username, password_hash, phone, email, wechat, qq, avatar, school, major, is_student, status) " +
            "VALUES (#{username}, #{passwordHash}, #{phone}, #{email}, #{wechat}, #{qq}, #{avatar}, #{school}, #{major}, #{isStudent}, 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);
    
    @Select("SELECT * FROM users WHERE phone = #{phone}")
    User selectByPhone(String phone);
    
    @Update("UPDATE users SET password_hash = #{passwordHash}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updatePassword(User user);
    
    @Update("UPDATE users SET avatar = #{avatar}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAvatar(User user);
    
    @Update("UPDATE users SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(User user);
}