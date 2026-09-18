package com.supermarket.backend.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Supplier {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String address;
    private String email;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
