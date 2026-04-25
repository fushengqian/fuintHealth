package com.fuint.module.merchantApi.controller;

import com.fuint.common.dto.member.UserDto;
import com.fuint.common.dto.member.UserInfo;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.param.MemberDetailParam;
import com.fuint.common.param.MemberInfoParam;
import com.fuint.common.param.MemberListParam;
import com.fuint.common.param.MemberPage;
import com.fuint.common.service.HealthRecordService;
import com.fuint.common.service.HealthReportService;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.StaffService;
import com.fuint.common.service.UserGradeService;
import com.fuint.common.util.DateUtil;
import com.fuint.common.util.TokenUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.web.BaseController;
import com.fuint.framework.web.ResponseObject;
import com.fuint.repository.model.MtStaff;
import com.fuint.repository.model.MtUser;
import com.fuint.repository.model.MtUserGrade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员管理类controller
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Api(tags="商户端-会员管理相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/merchantApi/member")
public class MerchantMemberController extends BaseController {

    /**
     * 会员服务接口
     */
    private MemberService memberService;

    /**
     * 店铺员工服务接口
     * */
    private StaffService staffService;

    /**
     * 会员等级服务接口
     **/
    private UserGradeService userGradeService;

    /**
     * 健康报告服务接口
     **/
    private HealthReportService healthReportService;

    /**
     * 健康记录服务接口
     **/
    private HealthRecordService healthRecordService;

    /**
     * 会员列表查询
     */
    @ApiOperation(value = "查询会员列表")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject list(@RequestBody MemberListParam memberListParam) throws BusinessCheckException {
        String dataType = memberListParam.getDataType();
        // 今日注册、今日活跃
        if (dataType.equals("todayRegister")) {
            String regTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd") + " 00:00:00~" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
            memberListParam.setRegTime(regTime);
        } else if (dataType.equals("todayActive")) {
            String activeTime = DateUtil.formatDate(new Date(), "yyyy-MM-dd") + " 00:00:00~" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
            memberListParam.setActiveTime(activeTime);
        }
        UserInfo userInfo = TokenUtil.getUserInfo();
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        MtStaff staffInfo = null;
        if (mtUser != null && mtUser.getMobile() != null) {
            staffInfo = staffService.queryStaffByMobile(mtUser.getMobile());
        }
        if (staffInfo == null) {
            return getFailureResult(201, "您的帐号不是商户，没有操作权限");
        }

        if (staffInfo.getMerchantId() != null && staffInfo.getMerchantId() > 0) {
            memberListParam.setMerchantId(staffInfo.getMerchantId());
        }
        if (staffInfo.getStoreId() != null && staffInfo.getStoreId() > 0) {
            memberListParam.setStoreId(staffInfo.getStoreId());
        }

        MemberPage memberPage = new MemberPage();
        BeanUtils.copyProperties(memberListParam, memberPage);
        PaginationResponse<UserDto> paginationResponse = memberService.queryMemberListByPagination(memberPage);

        // 会员等级列表
        List<MtUserGrade> userGradeList = userGradeService.getMerchantGradeList(staffInfo.getMerchantId(), StatusEnum.ENABLED.getKey());

        Map<String, Object> result = new HashMap<>();
        result.put("paginationResponse", paginationResponse);
        result.put("userGradeList", userGradeList);

        return getSuccessResult(result);
    }

    /**
     * 会员详情
     */
    @ApiOperation(value = "查询会员详情")
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject info(@RequestBody MemberDetailParam memberParam) throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();

        MtStaff staffInfo = null;
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        if (mtUser != null && mtUser.getMobile() != null) {
            staffInfo = staffService.queryStaffByMobile(mtUser.getMobile());
        }
        if (staffInfo == null) {
            return getFailureResult(201, "您的帐号不是商户，没有操作权限");
        }
        MtUser memberInfo = memberService.queryMemberById(memberParam.getMemberId());
        MtUserGrade gradeInfo = memberService.queryMemberGradeByGradeId(memberInfo.getGradeId());

        Map<String, Object> result = new HashMap<>();
        result.put("userInfo", memberInfo);
        result.put("gradeInfo", gradeInfo);

        return getSuccessResult(result);
    }

    /**
     * 保存会员信息
     */
    @ApiOperation(value = "保存会员信息")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject save(@RequestBody MemberInfoParam memberInfoParam) throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();

        MtStaff staffInfo = null;
        MtUser myUserInfo = memberService.queryMemberById(userInfo.getId());
        if (myUserInfo != null && myUserInfo.getMobile() != null) {
            staffInfo = staffService.queryStaffByMobile(myUserInfo.getMobile());
        }
        if (staffInfo == null) {
            return getFailureResult(201, "您的帐号不是商户，没有操作权限");
        }
        MtUser mtUser = new MtUser();
        if (memberInfoParam.getId() != null) {
            mtUser = memberService.queryMemberById(memberInfoParam.getId());
        }
        mtUser.setMerchantId(staffInfo.getMerchantId());
        mtUser.setStoreId(staffInfo.getStoreId());
        mtUser.setMobile(memberInfoParam.getMobile());
        mtUser.setName(memberInfoParam.getName());
        mtUser.setAvatar(memberInfoParam.getAvatar());
        mtUser.setSex(memberInfoParam.getSex());
        mtUser.setBirthday(memberInfoParam.getBirthday());
        mtUser.setUserNo(memberInfoParam.getUserNo());
        MtUser memberInfo;
        if (memberInfoParam.getId() == null) {
            mtUser.setDescription("商户登记添加");
            memberInfo = memberService.addMember(mtUser, null);
        } else {
            memberInfo = memberService.updateMember(mtUser, false);
        }
        return getSuccessResult(memberInfo);
    }

    /**
     * 导出会员健康报告
     */
    @ApiOperation(value = "导出会员健康报告")
    @RequestMapping(value = "/exportHealthReport", method = RequestMethod.GET)
    @CrossOrigin
    public void exportHealthReport(@RequestParam("memberId") Integer memberId, HttpServletResponse response) throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();

        MtStaff staffInfo = null;
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        if (mtUser != null && mtUser.getMobile() != null) {
            staffInfo = staffService.queryStaffByMobile(mtUser.getMobile());
        }
        if (staffInfo == null) {
            throw new BusinessCheckException("您的帐号不是商户，没有操作权限");
        }

        MtUser memberInfo = memberService.queryMemberById(memberId);
        if (memberInfo == null) {
            throw new BusinessCheckException("会员信息不存在");
        }

        if (!memberInfo.getMerchantId().equals(staffInfo.getMerchantId())) {
            throw new BusinessCheckException("您没有权限操作该会员");
        }

        healthReportService.generateHealthReportPdf(memberId, response);
    }

    /**
     * 保存会员体检数据
     */
    @ApiOperation(value = "保存会员体检数据")
    @RequestMapping(value = "/saveHealthRecord", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseObject saveHealthRecord(@RequestBody Map<String, Object> params) throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();

        MtStaff staffInfo = null;
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        if (mtUser != null && mtUser.getMobile() != null) {
            staffInfo = staffService.queryStaffByMobile(mtUser.getMobile());
        }
        if (staffInfo == null) {
            return getFailureResult(201, "您的帐号不是商户，没有操作权限");
        }

        Integer memberId = params.get("memberId") != null ? Integer.valueOf(params.get("memberId").toString()) : null;
        if (memberId == null) {
            return getFailureResult(201, "会员ID不能为空");
        }

        MtUser memberInfo = memberService.queryMemberById(memberId);
        if (memberInfo == null) {
            return getFailureResult(201, "会员信息不存在");
        }

        if (!memberInfo.getMerchantId().equals(staffInfo.getMerchantId())) {
            return getFailureResult(201, "您没有权限操作该会员");
        }

        com.fuint.repository.model.MtHealthRecord record = new com.fuint.repository.model.MtHealthRecord();
        record.setUserId(memberId);
        record.setOperator(userInfo.getUserName());

        if (params.get("id") != null) {
            record.setId(Integer.valueOf(params.get("id").toString()));
        }
        if (params.get("height") != null && !params.get("height").toString().isEmpty()) {
            record.setHeight(new java.math.BigDecimal(params.get("height").toString()));
        }
        if (params.get("weight") != null && !params.get("weight").toString().isEmpty()) {
            record.setWeight(new java.math.BigDecimal(params.get("weight").toString()));
        }
        if (params.get("bloodPressure") != null) {
            record.setBloodPressure(params.get("bloodPressure").toString());
        }
        if (params.get("bloodSugar") != null && !params.get("bloodSugar").toString().isEmpty()) {
            record.setBloodSugar(new java.math.BigDecimal(params.get("bloodSugar").toString()));
        }
        if (params.get("checkupDate") != null && !params.get("checkupDate").toString().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                record.setCheckupDate(sdf.parse(params.get("checkupDate").toString()));
            } catch (Exception e) {
                // 忽略日期解析错误，使用当前日期
            }
        }
        if (params.get("description") != null) {
            record.setDescription(params.get("description").toString());
        }

        record = healthRecordService.saveHealthRecord(record);
        return getSuccessResult(record);
    }

    /**
     * 查询会员最新体检数据
     */
    @ApiOperation(value = "查询会员最新体检数据")
    @RequestMapping(value = "/getLatestHealthRecord", method = RequestMethod.GET)
    @CrossOrigin
    public ResponseObject getLatestHealthRecord(@RequestParam("memberId") Integer memberId) throws BusinessCheckException {
        UserInfo userInfo = TokenUtil.getUserInfo();

        MtStaff staffInfo = null;
        MtUser mtUser = memberService.queryMemberById(userInfo.getId());
        if (mtUser != null && mtUser.getMobile() != null) {
            staffInfo = staffService.queryStaffByMobile(mtUser.getMobile());
        }
        if (staffInfo == null) {
            return getFailureResult(201, "您的帐号不是商户，没有操作权限");
        }

        MtUser memberInfo = memberService.queryMemberById(memberId);
        if (memberInfo == null) {
            return getFailureResult(201, "会员信息不存在");
        }

        if (!memberInfo.getMerchantId().equals(staffInfo.getMerchantId())) {
            return getFailureResult(201, "您没有权限操作该会员");
        }

        com.fuint.repository.model.MtHealthRecord record = healthRecordService.queryLatestByUserId(memberId);
        Map<String, Object> result = new HashMap<>();
        result.put("record", record);
        if (record != null && record.getHeight() != null && record.getWeight() != null) {
            result.put("bmi", healthRecordService.calculateBMI(record.getWeight(), record.getHeight()));
        } else {
            result.put("bmi", "暂无数据");
        }
        return getSuccessResult(result);
    }
}
