package com.example.cloud.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("account")
public class AccountDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private BigDecimal total;

    private BigDecimal used;

    private BigDecimal residue;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
