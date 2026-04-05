package com.example.cloud.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cloud.account.entity.AccountDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface AccountMapper extends BaseMapper<AccountDO> {

    @Select("SELECT id, user_id, total, used, residue, gmt_create, gmt_modified FROM account WHERE user_id = #{userId} LIMIT 1")
    AccountDO selectByUserId(@Param("userId") String userId);

    @Update("""
            UPDATE account
            SET used = used + #{money},
                residue = residue - #{money},
                gmt_modified = NOW()
            WHERE user_id = #{userId}
              AND residue >= #{money}
            """)
    int deduct(@Param("userId") String userId, @Param("money") BigDecimal money);
}
