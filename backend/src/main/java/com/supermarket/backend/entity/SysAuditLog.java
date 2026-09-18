package com.supermarket.backend.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SysAuditLog {
    private Long id;
    private String username;
    private String action;
    private String detail;
    private String ip;
    private Date createTime;
}
