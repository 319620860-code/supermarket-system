package com.supermarket.backend.mapper;

import com.supermarket.backend.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysAuditLogMapper {

    int insert(SysAuditLog row);

    long countByCondition(@Param("action") String action, @Param("usernameLike") String usernameLike);

    List<SysAuditLog> selectPageByCondition(
            @Param("action") String action,
            @Param("usernameLike") String usernameLike,
            @Param("offset") int offset,
            @Param("limit") int limit);
}
