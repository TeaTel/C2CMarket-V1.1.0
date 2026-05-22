package com.campus.backend.mapper;

import com.campus.backend.entity.OrgJoinRequest;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OrgJoinRequestMapper {

    @Insert("INSERT INTO org_join_requests (org_id, user_id, message) VALUES (#{orgId}, #{userId}, #{message})")
    int insert(OrgJoinRequest req);

    @Update("UPDATE org_join_requests SET status = #{status}, reviewer_id = #{reviewerId}, reviewed_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("reviewerId") Long reviewerId);

    @Select("SELECT * FROM org_join_requests WHERE org_id = #{orgId} AND status = 'PENDING' ORDER BY created_at ASC")
    List<OrgJoinRequest> selectPendingByOrgId(Long orgId);

    @Select("SELECT * FROM org_join_requests WHERE org_id = #{orgId} AND user_id = #{userId} AND status = 'PENDING'")
    OrgJoinRequest selectPendingByOrgAndUser(@Param("orgId") Long orgId, @Param("userId") Long userId);

    @Select("SELECT * FROM org_join_requests WHERE id = #{id}")
    OrgJoinRequest selectById(Long id);
}
