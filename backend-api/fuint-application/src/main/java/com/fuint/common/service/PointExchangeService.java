package com.fuint.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtPointExchange;

import java.util.List;
import java.util.Map;

public interface PointExchangeService extends IService<MtPointExchange> {

    List<Map<String, Object>> getExchangeableCouponList(Integer merchantId, Integer userId) throws BusinessCheckException;

    Map<String, Object> exchangeCoupon(Integer merchantId, Integer userId, Integer couponId, String operator) throws BusinessCheckException;

    boolean checkUserExchanged(Integer userId, Integer couponId);

    List<MtPointExchange> getUserExchangeList(Integer userId, Integer merchantId);
}
