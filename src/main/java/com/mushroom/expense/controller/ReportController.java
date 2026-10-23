package com.mushroom.expense.controller;

import com.mushroom.expense.service.ReportService;
import com.mushroom.expense.service.report.CsvReportWriter;
import com.mushroom.expense.service.report.ExcelReportWriter;
import com.mushroom.expense.service.report.ReportData.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;
    private final ExcelReportWriter excelReportWriter = new ExcelReportWriter();
    private final CsvReportWriter csvReportWriter = new CsvReportWriter();

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<?> downloadReport(
            @RequestParam(value = "type", defaultValue = "MONTHLY") String typeParam,
            @RequestParam(value = "fy", required = false) String fyParam,
            @RequestParam(value = "year", required = false) Integer yearParam,
            @RequestParam(value = "month", required = false) Integer monthParam,
            @RequestParam(value = "format", defaultValue = "XLSX") String formatParam,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = (userDetails != null) ? userDetails.getUsername() : "Anonymous";

        try {
            ReportRequest request = new ReportRequest();
            request.setType(ReportType.valueOf(typeParam.toUpperCase()));
            request.setFormat(ReportFormat.valueOf(formatParam.toUpperCase()));
            request.setFy(fyParam);
            request.setYear(yearParam);
            request.setMonth(monthParam);
            request.setRequestedBy(username);

            log.info("Report requested by [{}]: Type={}, FY={}, Year={}, Month={}, Format={}",
                    username, request.getType(), request.getFy(), request.getYear(), request.getMonth(), request.getFormat());

            ReportDataContainer data = reportService.compileReportData(request);

            byte[] fileBytes;
            String filename;
            MediaType mediaType;

            String periodLabel = (data.getSummary() != null && data.getSummary().getPeriodLabel() != null)
                    ? data.getSummary().getPeriodLabel()
                    : (data.getRequest() != null && data.getRequest().getPeriodLabel() != null
                            ? data.getRequest().getPeriodLabel()
                            : "Report");

            String sanitizedPeriod = periodLabel
                    .replace(" ", "_")
                    .replace("(", "")
                    .replace(")", "")
                    .replace("/", "-");

            if (request.getFormat() == ReportFormat.XLSX) {
                fileBytes = excelReportWriter.writeWorkbook(data);
                filename = String.format("MushroomFarm_%s_%s.xlsx", request.getType(), sanitizedPeriod);
                mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                fileBytes = csvReportWriter.writeZipArchive(data);
                filename = String.format("MushroomFarm_%s_%s_CSV.zip", request.getType(), sanitizedPeriod);
                mediaType = MediaType.parseMediaType("application/zip");
            }

            log.info("Report generated successfully for [{}]: File={}, Size={} bytes, ApprovedRecords={}",
                    username, filename, fileBytes.length,
                    data.getExpensesSection().getRecordCount() + data.getIncomeSection().getRecordCount() +
                            data.getCapitalSection().getRecordCount() + data.getLoansSection().getRecordCount());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(mediaType)
                    .contentLength(fileBytes.length)
                    .body(fileBytes);

        } catch (IllegalArgumentException ex) {
            log.warn("Report request rejected for [{}]: {}", username, ex.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Error generating report: " + ex.getMessage());
        } catch (IOException ex) {
            log.error("I/O Error while writing report for [{}]: {}", username, ex.getMessage(), ex);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Internal server error while building report file.");
        }
    }
}
