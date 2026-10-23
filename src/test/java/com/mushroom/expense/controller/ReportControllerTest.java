package com.mushroom.expense.controller;

import com.mushroom.expense.security.CustomUserDetailsService;
import com.mushroom.expense.service.ReportService;
import com.mushroom.expense.service.report.ReportData.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.mushroom.expense.config.SecurityConfig;

@WebMvcTest(ReportController.class)
@Import(SecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "acct", roles = {"ACCOUNTANT"})
    void testDownloadXlsxAsAccountant() throws Exception {
        ReportDataContainer mockContainer = createMockContainer(ReportType.MONTHLY, ReportFormat.XLSX);
        when(reportService.compileReportData(any(ReportRequest.class))).thenReturn(mockContainer);

        mockMvc.perform(get("/reports/download")
                        .param("type", "MONTHLY")
                        .param("year", "2025")
                        .param("month", "10")
                        .param("format", "XLSX"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("MushroomFarm_MONTHLY_")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.endsWith(".xlsx\"")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void testDownloadCsvAsAdmin() throws Exception {
        ReportDataContainer mockContainer = createMockContainer(ReportType.YEARLY, ReportFormat.CSV);
        when(reportService.compileReportData(any(ReportRequest.class))).thenReturn(mockContainer);

        mockMvc.perform(get("/reports/download")
                        .param("type", "YEARLY")
                        .param("fy", "2024-25")
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("MushroomFarm_YEARLY_")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.endsWith(".zip\"")))
                .andExpect(content().contentType("application/zip"));
    }

    @Test
    @WithMockUser(username = "supervisor_user", roles = {"SUPERVISOR"})
    void testDownloadDeniedForSupervisor() throws Exception {
        mockMvc.perform(get("/reports/download")
                        .param("type", "MONTHLY")
                        .param("year", "2025")
                        .param("month", "10")
                        .param("format", "XLSX"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDownloadDeniedForAnonymous() throws Exception {
        mockMvc.perform(get("/reports/download")
                        .param("type", "MONTHLY")
                        .param("year", "2025")
                        .param("month", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "acct", roles = {"ACCOUNTANT"})
    void testBadRequestHandlingForInvalidParameters() throws Exception {
        when(reportService.compileReportData(any(ReportRequest.class)))
                .thenThrow(new IllegalArgumentException("Cannot generate report for future period: Oct 2099"));

        mockMvc.perform(get("/reports/download")
                        .param("type", "MONTHLY")
                        .param("year", "2099")
                        .param("month", "10")
                        .param("format", "XLSX"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error generating report: Cannot generate report for future period")));
    }

    private ReportDataContainer createMockContainer(ReportType type, ReportFormat format) {
        ReportDataContainer c = new ReportDataContainer();
        ReportRequest req = new ReportRequest();
        req.setType(type);
        req.setFormat(format);
        req.setPeriodLabel("Test Period");
        req.setStartDate(LocalDate.of(2025, 10, 1));
        req.setEndDate(LocalDate.of(2025, 10, 31));
        c.setRequest(req);

        PeriodSummary s = new PeriodSummary();
        s.setPeriodLabel("Test Period");
        s.setStartDate(req.getStartDate());
        s.setEndDate(req.getEndDate());
        s.setGeneratedAt(LocalDateTime.now());
        s.setGeneratedBy("acct");
        s.setScheduleSummaries(Collections.emptyList());
        c.setSummary(s);

        LifetimeFinancials lf = new LifetimeFinancials();
        lf.setAsOfDate(LocalDateTime.now());
        lf.setGeneratedBy("acct");
        c.setLifetimeFinancials(lf);

        c.setExpensesSection(new ScheduleSection());
        c.setIncomeSection(new ScheduleSection());
        c.setCapitalSection(new ScheduleSection());
        c.setLoansSection(new ScheduleSection());
        return c;
    }
}
