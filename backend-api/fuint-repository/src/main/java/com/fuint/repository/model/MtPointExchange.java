package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("mt_point_exchange")
@ApiModel(value = "MtPointExchange对象", description = "积分兑换记录表")
public class MtPointExchange implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("店铺ID")
    private Integer storeId;

    @ApiModelProperty("会员ID")
    private Integer userId;

    @ApiModelProperty("优惠券ID")
    private Integer couponId;

    @ApiModelProperty("消耗积分")
    private Integer point;

    @ApiModelProperty("用户券ID")
    private Integer userCouponId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("备注说明")
    private String description;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("状态，A正常；D作废")
    private String status;

}
