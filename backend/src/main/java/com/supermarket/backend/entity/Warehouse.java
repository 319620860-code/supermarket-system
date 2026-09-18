package com.supermarket.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 仓库实体类
 */
@Data
public class Warehouse {
    /**
     * 仓库ID
     */
    private Long id;
    
    /**
     * 仓库名称
     */
    private String name;
    
    /**
     * 仓库类型（1-总仓，2-门店仓，3-退货仓）
     */
    private Integer type;
    
    /**
     * 仓库地址
     */
    private String address;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
