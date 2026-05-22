package com.campus.backend.mapper;

import com.campus.backend.entity.OrgMember;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OrgMemberMapper {

    @Insert("INSERT INTO org_members (org_id, user_id, role) VALUES (#{orgId}, #{userId}, #{role})")
    int insert(OrgMember member);

    @Delete("DELETE FROM org_members WHERE org_id = #{orgId} AND user_id = #{userId}")
    int delete(@Param("orgId") Long orgId, @Param("userId") Long userId);

    @Select("SELECT * FROM org_members WHERE org_id = #{orgId} AND user_id = #{userId}")
    OrgMember selectByOrgAndUser(@Param("orgId") Long orgId, @Param("userId") Long userId);

    @Select("SELECT * FROM org_members WHERE org_id = #{orgId} ORDER BY joined_at ASC LIMIT #{offset}, #{limit}")
    List<OrgMember> selectByOrgId(@Param("orgId") Long orgId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM org_members WHERE org_id = #{orgId}")
    int countByOrgId(Long orgId);

    @Update("UPDATE org_members SET role = #{role} WHERE org_id = #{orgId} AND user_id = #{userId}")
    int updateRole(@Param("orgId") Long orgId, @Param("userId") Long userId, @Param("role") String role);
}
