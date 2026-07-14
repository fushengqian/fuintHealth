package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.service.HealthRecordService;
import com.fuint.common.service.MemberService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.mapper.MtHealthRecordMapper;
import com.fuint.repository.model.MtHealthRecord;
import com.fuint.repository.model.MtUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * 会员健康记录服务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Slf4j
@Service
@AllArgsConstructor
public class HealthRecordServiceImpl extends ServiceImpl<MtHealthRecordMapper, MtHealthRecord> implements HealthRecordService {

    private MemberService memberService;

    private MtHealthRecordMapper mtHealthRecordMapper;

    @Override
    public List<MtHealthRecord> queryByUserId(Integer userId) {
        return mtHealthRecordMapper.queryByUserId(userId);
    }

    @Override
    public MtHealthRecord queryLatestByUserId(Integer userId) {
        return mtHealthRecordMapper.queryLatestByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MtHealthRecord saveHealthRecord(MtHealthRecord record) throws BusinessCheckException {
        if (record.getUserId() == null) {
            throw new BusinessCheckException("会员ID不能为空");
        }

        MtUser member = memberService.queryMemberById(record.getUserId());
        if (member == null) {
            throw new BusinessCheckException("会员信息不存在");
        }

        Date now = new Date();
        if (record.getId() == null || record.getId() <= 0) {
            record.setId(null);
            record.setMerchantId(member.getMerchantId());
            record.setStoreId(member.getStoreId());
            record.setStatus("A");
            record.setCreateTime(now);
            record.setUpdateTime(now);
            if (record.getCheckupDate() == null) {
                record.setCheckupDate(now);
            }
            this.save(record);
        } else {
            MtHealthRecord existRecord = this.getById(record.getId());
            if (existRecord == null) {
                throw new BusinessCheckException("健康记录不存在");
            }
            record.setUpdateTime(now);
            this.updateById(record);
        }

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHealthRecord(Integer id, String operator) throws BusinessCheckException {
        MtHealthRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessCheckException("健康记录不存在");
        }
        record.setStatus("D");
        record.setUpdateTime(new Date());
        record.setOperator(operator);
        this.updateById(record);
    }

    @Override
    public String calculateBMI(BigDecimal weight, BigDecimal height) {
        if (weight == null || height == null || weight.compareTo(BigDecimal.ZERO) <= 0 || height.compareTo(BigDecimal.ZERO) <= 0) {
            return "暂无数据";
        }

        try {
            BigDecimal heightM = height.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal heightSquare = heightM.multiply(heightM);
            BigDecimal bmi = weight.divide(heightSquare, 1, RoundingMode.HALF_UP);
            return bmi.toString();
        } catch (Exception e) {
            log.error("计算BMI失败", e);
            return "暂无数据";
        }
    }
}
