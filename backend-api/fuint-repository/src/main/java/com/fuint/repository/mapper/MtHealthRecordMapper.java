package com.fuint.repository.mapper;

import com.fuint.repository.model.MtHealthRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员健康记录 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtHealthRecordMapper extends BaseMapper<MtHealthRecord> {

    List<MtHealthRecord> queryByUserId(@Param("userId") Integer userId);

    MtHealthRecord queryLatestByUserId(@Param("userId") Integer userId);
}
