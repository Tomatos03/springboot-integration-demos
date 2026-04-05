package com.example.cloud.storage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("storage")
public class StorageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String commodityCode;

    private Integer total;

    private Integer used;

    private Integer residue;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;
}
