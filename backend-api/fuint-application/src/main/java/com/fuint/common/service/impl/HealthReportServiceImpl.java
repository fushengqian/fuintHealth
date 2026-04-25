package com.fuint.common.service.impl;

import com.fuint.common.service.HealthRecordService;
import com.fuint.common.service.HealthReportService;
import com.fuint.common.service.MemberService;
import com.fuint.common.util.DateUtil;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.repository.model.MtUser;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 健康报告服务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class HealthReportServiceImpl implements HealthReportService {

    private MemberService memberService;

    private HealthRecordService healthRecordService;

    @Override
    public void generateHealthReportPdf(Integer memberId, HttpServletResponse response) throws BusinessCheckException {
        MtUser memberInfo = memberService.queryMemberById(memberId);
        if (memberInfo == null) {
            throw new BusinessCheckException("会员信息不存在");
        }

        com.fuint.repository.model.MtHealthRecord latestRecord = healthRecordService.queryLatestByUserId(memberId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font chineseFont = loadChineseFont(document);
            float titleFontSize = 20;
            float headerFontSize = 14;
            float contentFontSize = 12;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = PDRectangle.A4.getWidth();
                float pageHeight = PDRectangle.A4.getHeight();
                float margin = 50;
                float yPosition = pageHeight - margin;

                contentStream.setNonStrokingColor(new Color(0, 102, 204));
                contentStream.setStrokingColor(new Color(0, 102, 204));
                contentStream.setLineWidth(2);
                contentStream.addRect(margin, pageHeight - margin - 40, pageWidth - 2 * margin, 40);
                contentStream.fill();

                if (chineseFont != null) {
                    contentStream.beginText();
                    contentStream.setFont(chineseFont, titleFontSize);
                    contentStream.setNonStrokingColor(Color.WHITE);
                    contentStream.newLineAtOffset(margin + 20, pageHeight - margin - 28);
                    contentStream.showText("会员健康报告");
                    contentStream.endText();
                } else {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
                    contentStream.setNonStrokingColor(Color.WHITE);
                    contentStream.newLineAtOffset(margin + 20, pageHeight - margin - 28);
                    contentStream.showText("Member Health Report");
                    contentStream.endText();
                }

                yPosition -= 70;

                contentStream.setNonStrokingColor(Color.BLACK);
                drawSection(contentStream, chineseFont, headerFontSize, "基本信息", "Basic Information", margin, yPosition);
                yPosition -= 35;

                String name = memberInfo.getName() != null ? memberInfo.getName() : "未填写";
                String mobile = memberInfo.getMobile() != null ? memberInfo.getMobile() : "未填写";
                
                String lastCheckupTime = "暂无记录";
                if (latestRecord != null && latestRecord.getCheckupDate() != null) {
                    lastCheckupTime = DateUtil.formatDate(latestRecord.getCheckupDate(), "yyyy-MM-dd");
                } else if (memberInfo.getUpdateTime() != null) {
                    lastCheckupTime = DateUtil.formatDate(memberInfo.getUpdateTime(), "yyyy-MM-dd");
                } else if (memberInfo.getCreateTime() != null) {
                    lastCheckupTime = DateUtil.formatDate(memberInfo.getCreateTime(), "yyyy-MM-dd");
                }
                
                String bmi = "暂无数据";
                if (latestRecord != null && latestRecord.getHeight() != null && latestRecord.getWeight() != null) {
                    bmi = healthRecordService.calculateBMI(latestRecord.getWeight(), latestRecord.getHeight());
                }

                drawRow(contentStream, chineseFont, contentFontSize, "姓名", "Name", name, margin, yPosition, pageWidth);
                yPosition -= 30;

                drawRow(contentStream, chineseFont, contentFontSize, "手机号", "Mobile", mobile, margin, yPosition, pageWidth);
                yPosition -= 30;

                drawRow(contentStream, chineseFont, contentFontSize, "最近体检时间", "Last Checkup", lastCheckupTime, margin, yPosition, pageWidth);
                yPosition -= 30;

                drawRow(contentStream, chineseFont, contentFontSize, "BMI指数", "BMI", bmi, margin, yPosition, pageWidth);
                yPosition -= 50;

                drawSection(contentStream, chineseFont, headerFontSize, "健康建议", "Health Suggestions", margin, yPosition);
                yPosition -= 35;

                String[] suggestions = {
                    "1. 保持规律作息，每天保证7-8小时睡眠",
                    "2. 均衡饮食，多摄入蔬菜水果",
                    "3. 每周进行至少150分钟中等强度有氧运动",
                    "4. 定期进行健康体检，关注身体变化",
                    "5. 保持良好心态，适当进行放松活动"
                };

                String[] suggestionsEn = {
                    "1. Keep regular sleep, 7-8 hours daily",
                    "2. Balanced diet, eat more fruits and vegetables",
                    "3. At least 150 minutes of moderate aerobic exercise weekly",
                    "4. Regular health check-ups",
                    "5. Keep a positive mindset"
                };

                for (int i = 0; i < suggestions.length; i++) {
                    if (chineseFont != null) {
                        drawText(contentStream, chineseFont, contentFontSize, suggestions[i], margin, yPosition);
                    } else {
                        drawText(contentStream, PDType1Font.HELVETICA, contentFontSize, suggestionsEn[i], margin, yPosition);
                    }
                    yPosition -= 25;
                }

                yPosition -= 20;
                contentStream.setStrokingColor(Color.LIGHT_GRAY);
                contentStream.setLineWidth(1);
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();

                yPosition -= 25;
                contentStream.setNonStrokingColor(Color.GRAY);
                String reportTime = "报告生成时间：" + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
                String reportTimeEn = "Generated: " + DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
                if (chineseFont != null) {
                    drawText(contentStream, chineseFont, 10, reportTime, margin, yPosition);
                } else {
                    drawText(contentStream, PDType1Font.HELVETICA, 10, reportTimeEn, margin, yPosition);
                }
            }

            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");
            String fileName = "健康报告_" + (memberInfo.getName() != null ? memberInfo.getName() : "会员") + ".pdf";
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()));

            document.save(response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessCheckException("生成PDF报告失败：" + e.getMessage());
        }
    }

    private PDType0Font loadChineseFont(PDDocument document) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] fontPaths;

            if (os.contains("win")) {
                fontPaths = new String[]{
                    "C:/Windows/Fonts/simsun.ttc",
                    "C:/Windows/Fonts/msyh.ttc",
                    "C:/Windows/Fonts/simhei.ttf"
                };
            } else if (os.contains("mac")) {
                fontPaths = new String[]{
                    "/System/Library/Fonts/PingFang.ttc",
                    "/System/Library/Fonts/STHeiti Light.ttc",
                    "/System/Library/Fonts/华文细黑.ttc"
                };
            } else {
                fontPaths = new String[]{
                    "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
                    "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                    "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
                };
            }

            for (String fontPath : fontPaths) {
                try {
                    File fontFile = new File(fontPath);
                    if (fontFile.exists()) {
                        return PDType0Font.load(document, fontFile);
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            try {
                InputStream fontStream = getClass().getResourceAsStream("/fonts/simsun.ttf");
                if (fontStream != null) {
                    return PDType0Font.load(document, fontStream);
                }
            } catch (Exception e) {
                // 忽略
            }

        } catch (Exception e) {
            // 忽略所有异常，返回null
        }
        return null;
    }

    private void drawSection(PDPageContentStream contentStream, PDType0Font chineseFont, float fontSize, 
                             String titleCn, String titleEn, float x, float y) throws IOException {
        if (chineseFont != null) {
            contentStream.beginText();
            contentStream.setFont(chineseFont, fontSize);
            contentStream.setNonStrokingColor(new Color(0, 102, 204));
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(titleCn);
            contentStream.endText();
        } else {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.setNonStrokingColor(new Color(0, 102, 204));
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(titleEn);
            contentStream.endText();
        }

        contentStream.setStrokingColor(new Color(0, 102, 204));
        contentStream.setLineWidth(2);
        contentStream.moveTo(x, y - 8);
        contentStream.lineTo(x + 80, y - 8);
        contentStream.stroke();
    }

    private void drawRow(PDPageContentStream contentStream, PDType0Font chineseFont, float fontSize,
                         String labelCn, String labelEn, String value, float x, float y, float pageWidth) throws IOException {
        contentStream.setNonStrokingColor(new Color(102, 102, 102));
        if (chineseFont != null) {
            contentStream.beginText();
            contentStream.setFont(chineseFont, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(labelCn + "：");
            contentStream.endText();
        } else {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(labelEn + ":");
            contentStream.endText();
        }

        contentStream.setNonStrokingColor(Color.BLACK);
        if (chineseFont != null) {
            contentStream.beginText();
            contentStream.setFont(chineseFont, fontSize);
            contentStream.newLineAtOffset(x + 100, y);
            contentStream.showText(value);
            contentStream.endText();
        } else {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(x + 100, y);
            contentStream.showText(value);
            contentStream.endText();
        }

        contentStream.setStrokingColor(new Color(240, 240, 240));
        contentStream.setLineWidth(1);
        contentStream.moveTo(x, y - 10);
        contentStream.lineTo(pageWidth - 50, y - 10);
        contentStream.stroke();
    }

    private void drawText(PDPageContentStream contentStream, Object font, float fontSize, String text, float x, float y) throws IOException {
        contentStream.beginText();
        if (font instanceof PDType0Font) {
            contentStream.setFont((PDType0Font) font, fontSize);
        } else {
            contentStream.setFont((PDType1Font) font, fontSize);
        }
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }
}
