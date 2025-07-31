package com.demo.mapper;

import com.demo.entity.TRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author : Tomatos
 * @date : 2025/7/29
 */
@Mapper
public interface RoleMapper {
    List<TRole> queryRolesByUserId(Integer id);
}
