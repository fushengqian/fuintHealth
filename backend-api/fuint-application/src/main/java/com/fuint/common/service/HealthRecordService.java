package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtHealthRecord;

import java.util.List;

/**
 * 会员健康记录服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface HealthRecordService extends IService<MtHealthRecord> {

    /**
     * 根据会员ID查询健康记录列表
     *
     * @param userId 会员ID
     * @return 健康记录列表
     */
    List<MtHealthRecord> queryByUserId(Integer userId);

    /**
     * 根据会员ID查询最新的健康记录
     *
     * @param userId 会员ID
     * @return 最新的健康记录
     */
    MtHealthRecord queryLatestByUserId(Integer userId);

    /**
     * 保存健康记录
     *
     * @param record 健康记录
     * @return 保存后的健康记录
     * @throws BusinessCheckException
     */
    MtHealthRecord saveHealthRecord(MtHealthRecord record) throws BusinessCheckException;

    /**
     * 根据ID删除健康记录（逻辑删除）
     *
     * @param id 记录ID
     * @param operator 操作人
     * @throws BusinessCheckException
     */
    void deleteHealthRecord(Integer id, String operator) throws BusinessCheckException;

    /**
     * 计算BMI
     *
     * @param weight 体重(kg)
     * @param height 身高(cm)
     * @return BMI值，保留1位小数
     */
    String calculateBMI(java.math.BigDecimal weight, java.math.BigDecimal height);
}
