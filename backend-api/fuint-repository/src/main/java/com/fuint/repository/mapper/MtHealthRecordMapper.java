package com.fuint.repository.mapper;

import com.fuint.repository.model.MtHealthRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会员健康记录 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtHealthRecordMapper extends BaseMapper<MtHealthRecord> {

    @Select("SELECT * FROM mt_health_record t WHERE t.USER_ID = #{userId} AND t.STATUS = 'A' ORDER BY t.CHECKUP_DATE DESC, t.CREATE_TIME DESC")
    List<MtHealthRecord> queryByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM mt_health_record t WHERE t.USER_ID = #{userId} AND t.STATUS = 'A' ORDER BY t.CHECKUP_DATE DESC, t.CREATE_TIME DESC LIMIT 1")
    MtHealthRecord queryLatestByUserId(@Param("userId") Integer userId);
}
