package com.demo.mapper;

import com.demo.entity.TUser;
import org.apache.ibatis.annotations.Select;

/**
 * @author : Tomatos
 * @date : 2025/7/29
 */
public interface TUserMapper {
    @Select("SELECT * FROM t_user WHERE login_act = #{username}")
    TUser queryByUsername(String username);
}
