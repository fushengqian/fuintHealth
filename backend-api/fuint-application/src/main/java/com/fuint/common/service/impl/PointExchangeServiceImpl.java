package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.UserCouponStatusEnum;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.PointExchangeService;
import com.fuint.common.service.PointService;
import com.fuint.common.util.SeqUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.mapper.MtCouponMapper;
import com.fuint.repository.mapper.MtPointExchangeMapper;
import com.fuint.repository.mapper.MtPointMapper;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.mapper.MtUserMapper;
import com.fuint.repository.model.MtCoupon;
import com.fuint.repository.model.MtPoint;
import com.fuint.repository.model.MtPointExchange;
import com.fuint.repository.model.MtUser;
import com.fuint.repository.model.MtUserCoupon;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor(onConstructor_= {@Lazy})
public class PointExchangeServiceImpl extends ServiceImpl<MtPointExchangeMapper, MtPointExchange> implements PointExchangeService {

    private static final Logger logger = LoggerFactory.getLogger(PointExchangeServiceImpl.class);

    private MtPointExchangeMapper mtPointExchangeMapper;

    private MtCouponMapper mtCouponMapper;

    private MtUserCouponMapper mtUserCouponMapper;

    private MtUserMapper mtUserMapper;

    private MtPointMapper mtPointMapper;

    private PointService pointService;

    private MemberService memberService;

    @Override
    public List<Map<String, Object>> getExchangeableCouponList(Integer merchantId, Integer userId) throws BusinessCheckException {
        List<Map<String, Object>> result = new ArrayList<>();

        LambdaQueryWrapper<MtCoupon> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(MtCoupon::getStatus, StatusEnum.ENABLED.getKey());
        lambdaQueryWrapper.gt(MtCoupon::getPoint, 0);
        if (merchantId != null && merchantId > 0) {
            lambdaQueryWrapper.eq(MtCoupon::getMerchantId, merchantId);
        }
        lambdaQueryWrapper.orderByDesc(MtCoupon::getId);

        List<MtCoupon> couponList = mtCouponMapper.selectList(lambdaQueryWrapper);

        for (MtCoupon coupon : couponList) {
            if (!isCouponEffective(coupon)) {
                continue;
            }

            if (checkUserExchanged(userId, coupon.getId())) {
                continue;
            }

            Long sentNum = mtUserCouponMapper.getSendNum(coupon.getId());
            int leftNum = coupon.getTotal() - sentNum.intValue();
            if (leftNum <= 0) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", coupon.getId());
            item.put("name", coupon.getName());
            item.put("point", coupon.getPoint());
            item.put("amount", coupon.getAmount());
            item.put("type", coupon.getType());
            item.put("leftNum", leftNum);
            item.put("total", coupon.getTotal());
            item.put("limitNum", coupon.getLimitNum());
            item.put("description", coupon.getDescription());
            item.put("image", coupon.getImage());
            item.put("expireType", coupon.getExpireType());
            item.put("expireTime", coupon.getExpireTime());
            item.put("beginTime", coupon.getBeginTime());
            item.put("endTime", coupon.getEndTime());

            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> exchangeCoupon(Integer merchantId, Integer userId, Integer couponId, String operator) throws BusinessCheckException {
        MtUser userInfo = mtUserMapper.selectById(userId);
        if (userInfo == null) {
            throw new BusinessCheckException("会员信息不存在");
        }

        MtCoupon couponInfo = mtCouponMapper.selectById(couponId);
        if (couponInfo == null) {
            throw new BusinessCheckException("优惠券信息不存在");
        }

        if (!couponInfo.getStatus().equals(StatusEnum.ENABLED.getKey())) {
            throw new BusinessCheckException("该优惠券已停用，不能兑换");
        }

        if (couponInfo.getPoint() == null || couponInfo.getPoint() <= 0) {
            throw new BusinessCheckException("该优惠券不支持积分兑换");
        }

        if (!isCouponEffective(couponInfo)) {
            throw new BusinessCheckException("该优惠券已过期，不能兑换");
        }

        if (checkUserExchanged(userId, couponId)) {
            throw new BusinessCheckException("您已兑换过该优惠券，不能重复兑换");
        }

        Long sentNum = mtUserCouponMapper.getSendNum(couponId);
        int leftNum = couponInfo.getTotal() - sentNum.intValue();
        if (leftNum <= 0) {
            throw new BusinessCheckException("该优惠券已兑换完毕");
        }

        Integer userPoints = userInfo.getPoint() == null ? 0 : userInfo.getPoint();
        if (userPoints < couponInfo.getPoint()) {
            throw new BusinessCheckException("积分不足，当前积分：" + userPoints + "，所需积分：" + couponInfo.getPoint());
        }

        MtPoint deductPoint = new MtPoint();
        deductPoint.setUserId(userId);
        deductPoint.setAmount(-couponInfo.getPoint());
        deductPoint.setDescription("积分兑换优惠券：" + couponInfo.getName());
        deductPoint.setOperator(operator);
        pointService.addPoint(deductPoint);

        MtUserCoupon userCoupon = createUserCoupon(couponInfo, userInfo, operator);

        MtPointExchange exchange = new MtPointExchange();
        exchange.setMerchantId(merchantId);
        exchange.setStoreId(userInfo.getStoreId());
        exchange.setUserId(userId);
        exchange.setCouponId(couponId);
        exchange.setUserCouponId(userCoupon.getId());
        exchange.setPoint(couponInfo.getPoint());
        exchange.setStatus(StatusEnum.ENABLED.getKey());
        exchange.setCreateTime(new Date());
        exchange.setUpdateTime(new Date());
        exchange.setOperator(operator);
        exchange.setDescription("积分兑换优惠券：" + couponInfo.getName());
        mtPointExchangeMapper.insert(exchange);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "兑换成功");
        result.put("couponName", couponInfo.getName());
        result.put("userCouponId", userCoupon.getId());
        result.put("code", userCoupon.getCode());
        result.put("deductPoint", couponInfo.getPoint());
        result.put("remainingPoint", userInfo.getPoint() - couponInfo.getPoint());

        return result;
    }

    @Override
    public boolean checkUserExchanged(Integer userId, Integer couponId) {
        if (userId == null || couponId == null) {
            return false;
        }

        LambdaQueryWrapper<MtPointExchange> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.eq(MtPointExchange::getUserId, userId);
        lambdaQueryWrapper.eq(MtPointExchange::getCouponId, couponId);
        lambdaQueryWrapper.eq(MtPointExchange::getStatus, StatusEnum.ENABLED.getKey());

        Integer count = mtPointExchangeMapper.selectCount(lambdaQueryWrapper);
        return count != null && count > 0;
    }

    @Override
    public List<MtPointExchange> getUserExchangeList(Integer userId, Integer merchantId) {
        LambdaQueryWrapper<MtPointExchange> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (userId != null && userId > 0) {
            lambdaQueryWrapper.eq(MtPointExchange::getUserId, userId);
        }
        if (merchantId != null && merchantId > 0) {
            lambdaQueryWrapper.eq(MtPointExchange::getMerchantId, merchantId);
        }
        lambdaQueryWrapper.eq(MtPointExchange::getStatus, StatusEnum.ENABLED.getKey());
        lambdaQueryWrapper.orderByDesc(MtPointExchange::getId);

        return mtPointExchangeMapper.selectList(lambdaQueryWrapper);
    }

    private boolean isCouponEffective(MtCoupon coupon) {
        if (coupon == null) {
            return false;
        }

        if (!coupon.getStatus().equals(StatusEnum.ENABLED.getKey())) {
            return false;
        }

        Date now = new Date();

        if (coupon.getExpireType() != null && "FIX".equals(coupon.getExpireType())) {
            if (coupon.getBeginTime() != null && now.before(coupon.getBeginTime())) {
                return false;
            }
            if (coupon.getEndTime() != null && now.after(coupon.getEndTime())) {
                return false;
            }
        }

        return true;
    }

    private MtUserCoupon createUserCoupon(MtCoupon couponInfo, MtUser userInfo, String operator) {
        MtUserCoupon userCoupon = new MtUserCoupon();
        userCoupon.setCouponId(couponInfo.getId());
        userCoupon.setType(couponInfo.getType());
        userCoupon.setImage(couponInfo.getImage());
        userCoupon.setMerchantId(couponInfo.getMerchantId());
        userCoupon.setStoreId(userInfo.getStoreId());
        userCoupon.setAmount(couponInfo.getAmount());
        userCoupon.setBalance(couponInfo.getAmount());
        userCoupon.setOperator(operator);
        userCoupon.setGroupId(couponInfo.getGroupId());
        userCoupon.setMobile(userInfo.getMobile());
        userCoupon.setUserId(userInfo.getId());
        userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getKey());
        userCoupon.setCreateTime(new Date());
        userCoupon.setUpdateTime(new Date());

        if ("FIX".equals(couponInfo.getExpireType())) {
            userCoupon.setExpireTime(couponInfo.getEndTime());
        }
        if ("FLEX".equals(couponInfo.getExpireType()) && couponInfo.getExpireTime() != null) {
            Date expireTime = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(expireTime);
            c.add(Calendar.DATE, couponInfo.getExpireTime());
            expireTime = c.getTime();
            userCoupon.setExpireTime(expireTime);
        }

        userCoupon.setCode(SeqUtil.getRandomNumber(12));
        mtUserCouponMapper.insert(userCoupon);

        return userCoupon;
    }
}
