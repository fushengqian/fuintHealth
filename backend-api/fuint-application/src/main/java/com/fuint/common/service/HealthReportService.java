package com.fuint.common.service;

import com.fuint.framework.exception.BusinessCheckException;

import javax.servlet.http.HttpServletResponse;

/**
 * 健康报告服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface HealthReportService {

    /**
     * 生成会员健康报告 PDF
     *
     * @param memberId 会员ID
     * @param response HTTP响应
     * @throws BusinessCheckException
     */
    void generateHealthReportPdf(Integer memberId, HttpServletResponse response) throws BusinessCheckException;
}
