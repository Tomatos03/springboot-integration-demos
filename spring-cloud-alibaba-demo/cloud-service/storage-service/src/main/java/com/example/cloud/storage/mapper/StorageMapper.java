package com.example.cloud.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.cloud.storage.entity.StorageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StorageMapper extends BaseMapper<StorageDO> {

    @Select("SELECT id, commodity_code, total, used, residue, gmt_create, gmt_modified FROM storage WHERE commodity_code = #{commodityCode} LIMIT 1")
    StorageDO selectByCommodityCode(@Param("commodityCode") String commodityCode);

    @Update("""
            UPDATE storage
            SET used = used + #{count},
                residue = residue - #{count},
                gmt_modified = NOW()
            WHERE commodity_code = #{commodityCode}
              AND residue >= #{count}
            """)
    int deduct(@Param("commodityCode") String commodityCode, @Param("count") Integer count);
}
