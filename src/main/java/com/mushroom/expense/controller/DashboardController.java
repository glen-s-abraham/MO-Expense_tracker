package com.mushroom.expense.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.repository.ExpenseRepository;
import com.mushroom.expense.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String graphicalDashboard(
            @RequestParam(value = "year", required = false) Integer yearParam,
            @RequestParam(value = "month", required = false) Integer monthParam,
            @RequestParam(value = "fy", required = false) String fyParam,
            @RequestParam(value = "activeTab", defaultValue = "monthly") String activeTab,
            Model model) throws JsonProcessingException {
        
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        
        // Month/Year params for Monthly Tab
        int year = (yearParam != null) ? yearParam : currentYear;
        int month = (monthParam != null) ? monthParam : now.getMonthValue();
        
        // FY params for Yearly Tab
        String defaultFy = now.getMonthValue() >= 4 ? currentYear + "-" + (currentYear + 1) : (currentYear - 1) + "-" + currentYear;
        String fy = (fyParam != null && !fyParam.isEmpty()) ? fyParam : defaultFy;
        
        model.addAttribute("activeTab", activeTab);

        // -------------------------------------------------------------
        // SECTION 1: GLOBAL LIFETIME TOTALS (from commencement)
        // -------------------------------------------------------------
        List<Object[]> globalExpensesByCategory = expenseRepository.findSumByCategory(ExpenseStatus.APPROVED);
        List<Object[]> globalIncomesByCategory = incomeRepository.findSumByCategory(IncomeStatus.APPROVED);

        double globalTotalExpense = globalExpensesByCategory.stream().mapToDouble(obj -> (Double) obj[1]).sum();
        double globalTotalIncome = globalIncomesByCategory.stream().mapToDouble(obj -> (Double) obj[1]).sum();

        double globalTotalFixedAssets = 0.0;
        double globalTotalCurrentAssets = 0.0;

        for (Object[] obj : globalExpensesByCategory) {
            String catName = ((String) obj[0]).toLowerCase();
            Double amt = (Double) obj[1];
            if (catName.contains("equipment") || catName.contains("machinery") || catName.contains("building") || catName.contains("vehicle") || catName.contains("asset") || catName.contains("capital")) {
                globalTotalFixedAssets += amt;
            } else if (catName.contains("inventory") || catName.contains("cash") || catName.contains("bank") || catName.contains("receivable")) {
                globalTotalCurrentAssets += amt;
            }
        }
        double globalSalesIncome = 0.0;
        double globalCapitalLoansOtherIncome = 0.0;
        for (Object[] obj : globalIncomesByCategory) {
            String catName = ((String) obj[0]).toLowerCase();
            Double amt = (Double) obj[1];
            if (catName.contains("equipment") || catName.contains("machinery") || catName.contains("building") || catName.contains("vehicle") || catName.contains("asset") || catName.contains("capital")) {
                globalTotalFixedAssets += amt;
            } else if (catName.contains("inventory") || catName.contains("cash") || catName.contains("bank") || catName.contains("receivable") || catName.contains("loan")) {
                globalTotalCurrentAssets += amt;
            }
            
            if (catName.contains("sale")) {
                globalSalesIncome += amt;
            } else {
                globalCapitalLoansOtherIncome += amt;
            }
        }

        model.addAttribute("globalTotalExpense", globalTotalExpense);
        model.addAttribute("globalTotalIncome", globalTotalIncome);
        model.addAttribute("globalSalesIncome", globalSalesIncome);
        model.addAttribute("globalCapitalLoansOtherIncome", globalCapitalLoansOtherIncome);
        model.addAttribute("globalNetBalance", globalTotalIncome - globalTotalExpense);
        model.addAttribute("globalTotalFixedAssets", globalTotalFixedAssets);
        model.addAttribute("globalTotalCurrentAssets", globalTotalCurrentAssets);


        // -------------------------------------------------------------
        // SECTION 2: MONTHLY SNAPSHOT (Tab 1)
        // -------------------------------------------------------------
        YearMonth selectedMonth = YearMonth.of(year, month);
        LocalDate startDate = selectedMonth.atDay(1);
        LocalDate endDate = selectedMonth.atEndOfMonth();

        List<Object[]> monthlyExpensesByCategory = expenseRepository.findSumByCategoryAndDateBetween(ExpenseStatus.APPROVED, startDate, endDate);
        List<Object[]> monthlyIncomesByCategory = incomeRepository.findSumByCategoryAndDateBetween(IncomeStatus.APPROVED, startDate, endDate);

        double monthlyTotalExpense = monthlyExpensesByCategory.stream().mapToDouble(obj -> (Double) obj[1]).sum();
        double monthlyTotalIncome = monthlyIncomesByCategory.stream().mapToDouble(obj -> (Double) obj[1]).sum();

        double monthlySalesIncome = 0.0;
        double monthlyCapitalLoansOtherIncome = 0.0;
        for (Object[] obj : monthlyIncomesByCategory) {
            String catName = ((String) obj[0]).toLowerCase();
            Double amt = (Double) obj[1];
            if (catName.contains("sale")) {
                monthlySalesIncome += amt;
            } else {
                monthlyCapitalLoansOtherIncome += amt;
            }
        }

        model.addAttribute("monthlyTotalExpense", monthlyTotalExpense);
        model.addAttribute("monthlyTotalIncome", monthlyTotalIncome);
        model.addAttribute("monthlySalesIncome", monthlySalesIncome);
        model.addAttribute("monthlyCapitalLoansOtherIncome", monthlyCapitalLoansOtherIncome);
        model.addAttribute("monthlyNetBalance", monthlyTotalIncome - monthlyTotalExpense);
        
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedMonthName", selectedMonth.getMonth().name());

        List<Integer> availableYears = new ArrayList<>();
        for (int i = 2023; i <= currentYear + 1; i++) {
            availableYears.add(i);
        }
        model.addAttribute("availableYears", availableYears);
        
        Map<Integer, String> availableMonths = new HashMap<>();
        for (Month m : Month.values()) {
            availableMonths.put(m.getValue(), m.name());
        }
        model.addAttribute("availableMonths", availableMonths);

        // Doughnut Data (Monthly)
        List<String> expenseCategories = new ArrayList<>();
        List<Double> expenseAmounts = new ArrayList<>();
        for (Object[] obj : monthlyExpensesByCategory) {
            expenseCategories.add((String) obj[0]);
            expenseAmounts.add((Double) obj[1]);
        }

        List<String> incomeCategories = new ArrayList<>();
        List<Double> incomeAmounts = new ArrayList<>();
        for (Object[] obj : monthlyIncomesByCategory) {
            incomeCategories.add((String) obj[0]);
            incomeAmounts.add((Double) obj[1]);
        }

        model.addAttribute("expenseCategoriesJson", objectMapper.writeValueAsString(expenseCategories));
        model.addAttribute("expenseAmountsJson", objectMapper.writeValueAsString(expenseAmounts));
        model.addAttribute("incomeCategoriesJson", objectMapper.writeValueAsString(incomeCategories));
        model.addAttribute("incomeAmountsJson", objectMapper.writeValueAsString(incomeAmounts));

        // Line Chart Data (Monthly)
        List<Object[]> dailyExpenses = expenseRepository.findDailySumByDateBetween(ExpenseStatus.APPROVED, startDate, endDate);
        List<Object[]> dailyIncomes = incomeRepository.findDailySumByDateBetween(IncomeStatus.APPROVED, startDate, endDate);

        Map<Integer, Double> expenseMap = new HashMap<>();
        for (Object[] obj : dailyExpenses) {
            LocalDate dateVal = parseDate(obj[0]);
            expenseMap.put(dateVal.getDayOfMonth(), (Double) obj[1]);
        }

        Map<Integer, Double> incomeMap = new HashMap<>();
        for (Object[] obj : dailyIncomes) {
            LocalDate dateVal = parseDate(obj[0]);
            incomeMap.put(dateVal.getDayOfMonth(), (Double) obj[1]);
        }

        List<Integer> days = new ArrayList<>();
        List<Double> dailyExpenseAmounts = new ArrayList<>();
        List<Double> dailyIncomeAmounts = new ArrayList<>();

        for (int i = 1; i <= selectedMonth.lengthOfMonth(); i++) {
            days.add(i);
            dailyExpenseAmounts.add(expenseMap.getOrDefault(i, 0.0));
            dailyIncomeAmounts.add(incomeMap.getOrDefault(i, 0.0));
        }

        model.addAttribute("daysJson", objectMapper.writeValueAsString(days));
        model.addAttribute("dailyExpenseAmountsJson", objectMapper.writeValueAsString(dailyExpenseAmounts));
        model.addAttribute("dailyIncomeAmountsJson", objectMapper.writeValueAsString(dailyIncomeAmounts));


        // -------------------------------------------------------------
        // SECTION 3: YEARLY SNAPSHOT (Tab 2)
        // -------------------------------------------------------------
        int fyStartYear = Integer.parseInt(fy.split("-")[0]);
        LocalDate fyStartDate = LocalDate.of(fyStartYear, 4, 1);
        LocalDate fyEndDate = LocalDate.of(fyStartYear + 1, 3, 31);
        
        List<String> availableFys = new ArrayList<>();
        for (int i = 2023; i <= currentYear + 1; i++) {
            availableFys.add(i + "-" + (i + 1));
        }
        model.addAttribute("availableFys", availableFys);
        model.addAttribute("selectedFy", fy);
        
        List<Object[]> fyExpensesByCategory = expenseRepository.findSumByCategoryAndDateBetween(ExpenseStatus.APPROVED, fyStartDate, fyEndDate);
        List<Object[]> fyIncomesByCategory = incomeRepository.findSumByCategoryAndDateBetween(IncomeStatus.APPROVED, fyStartDate, fyEndDate);

        double fyTotalExpense = fyExpensesByCategory.stream().mapToDouble(obj -> (Double) obj[1]).sum();
        double fyTotalIncome = fyIncomesByCategory.stream().mapToDouble(obj -> (Double) obj[1]).sum();

        double fySalesIncome = 0.0;
        double fyCapitalLoansOtherIncome = 0.0;
        for (Object[] obj : fyIncomesByCategory) {
            String catName = ((String) obj[0]).toLowerCase();
            Double amt = (Double) obj[1];
            if (catName.contains("sale")) {
                fySalesIncome += amt;
            } else {
                fyCapitalLoansOtherIncome += amt;
            }
        }

        model.addAttribute("fyTotalExpense", fyTotalExpense);
        model.addAttribute("fyTotalIncome", fyTotalIncome);
        model.addAttribute("fySalesIncome", fySalesIncome);
        model.addAttribute("fyCapitalLoansOtherIncome", fyCapitalLoansOtherIncome);
        model.addAttribute("fyNetBalance", fyTotalIncome - fyTotalExpense);

        // Doughnut Data (Yearly)
        List<String> fyExpenseCategories = new ArrayList<>();
        List<Double> fyExpenseAmounts = new ArrayList<>();
        for (Object[] obj : fyExpensesByCategory) {
            fyExpenseCategories.add((String) obj[0]);
            fyExpenseAmounts.add((Double) obj[1]);
        }

        List<String> fyIncomeCategories = new ArrayList<>();
        List<Double> fyIncomeAmounts = new ArrayList<>();
        for (Object[] obj : fyIncomesByCategory) {
            fyIncomeCategories.add((String) obj[0]);
            fyIncomeAmounts.add((Double) obj[1]);
        }

        model.addAttribute("fyExpenseCategoriesJson", objectMapper.writeValueAsString(fyExpenseCategories));
        model.addAttribute("fyExpenseAmountsJson", objectMapper.writeValueAsString(fyExpenseAmounts));
        model.addAttribute("fyIncomeCategoriesJson", objectMapper.writeValueAsString(fyIncomeCategories));
        model.addAttribute("fyIncomeAmountsJson", objectMapper.writeValueAsString(fyIncomeAmounts));
        
        // Line Chart Data (Yearly - Aggregated by Month)
        List<Object[]> fyDailyExpenses = expenseRepository.findDailySumByDateBetween(ExpenseStatus.APPROVED, fyStartDate, fyEndDate);
        List<Object[]> fyDailyIncomes = incomeRepository.findDailySumByDateBetween(IncomeStatus.APPROVED, fyStartDate, fyEndDate);
        
        List<String> fyMonths = Arrays.asList("Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar");
        List<Double> fyMonthlyExpenseAmounts = new ArrayList<>(Collections.nCopies(12, 0.0));
        List<Double> fyMonthlyIncomeAmounts = new ArrayList<>(Collections.nCopies(12, 0.0));

        for (Object[] obj : fyDailyExpenses) {
            LocalDate dateVal = parseDate(obj[0]);
            int monthValue = dateVal.getMonthValue();
            int index = (monthValue >= 4) ? monthValue - 4 : monthValue + 8;
            fyMonthlyExpenseAmounts.set(index, fyMonthlyExpenseAmounts.get(index) + (Double) obj[1]);
        }
        for (Object[] obj : fyDailyIncomes) {
            LocalDate dateVal = parseDate(obj[0]);
            int monthValue = dateVal.getMonthValue();
            int index = (monthValue >= 4) ? monthValue - 4 : monthValue + 8;
            fyMonthlyIncomeAmounts.set(index, fyMonthlyIncomeAmounts.get(index) + (Double) obj[1]);
        }

        model.addAttribute("fyMonthsJson", objectMapper.writeValueAsString(fyMonths));
        model.addAttribute("fyMonthlyExpenseAmountsJson", objectMapper.writeValueAsString(fyMonthlyExpenseAmounts));
        model.addAttribute("fyMonthlyIncomeAmountsJson", objectMapper.writeValueAsString(fyMonthlyIncomeAmounts));

        return "dashboard";
    }

    private LocalDate parseDate(Object obj) {
        if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        } else if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime().toLocalDate();
        } else {
            return (LocalDate) obj;
        }
    }
}
