package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.service.CapitalService;
import com.mushroom.expense.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CapitalTransactionRepository capitalTransactionRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private LoanService loanService;

    @GetMapping("/category-breakdown")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCategoryBreakdownAndDateRange(
            @RequestParam("type") String type,
            @RequestParam("category") String category,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> response = new ArrayList<>();

        if ("INCOME".equalsIgnoreCase(type)) {
            List<Income> incomes = incomeRepository.findByCategoryNameAndStatusAndDateBetweenOrderByDateDesc(
                    category, IncomeStatus.APPROVED, startDate, endDate);
            response = incomes.stream().map(i -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", i.getId());
                map.put("description", i.getDescription());
                map.put("amount", i.getAmount());
                map.put("date", i.getDate());
                return map;
            }).collect(Collectors.toList());
        } else if ("CAPITAL_INTRODUCED".equalsIgnoreCase(type) || "CAPITAL_DRAWINGS".equalsIgnoreCase(type)) {
            CapitalTransactionType capType = "CAPITAL_INTRODUCED".equalsIgnoreCase(type)
                    ? CapitalTransactionType.INTRODUCED
                    : CapitalTransactionType.DRAWINGS;
            List<CapitalTransaction> txs = capitalTransactionRepository
                    .findByTypeAndStatusAndDateBetweenOrderByDateDesc(capType, IncomeStatus.APPROVED, startDate, endDate);
            response = txs.stream().map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("description", c.getDescription());
                map.put("amount", c.getAmount());
                map.put("date", c.getDate());
                return map;
            }).collect(Collectors.toList());
        } else if ("LOAN_BORROWED".equalsIgnoreCase(type) || "LOAN_REPAYMENT".equalsIgnoreCase(type)) {
            LoanTransactionType loanType = "LOAN_BORROWED".equalsIgnoreCase(type)
                    ? LoanTransactionType.BORROWED
                    : LoanTransactionType.REPAYMENT;
            List<LoanTransaction> txs = loanTransactionRepository
                    .findByTypeAndStatusAndDateBetweenOrderByDateDesc(loanType, IncomeStatus.APPROVED, startDate, endDate);
            response = txs.stream().map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", t.getId());
                map.put("description", t.getLoan().getLoanName() + " - " + t.getDescription());
                map.put("amount", t.getAmount());
                map.put("date", t.getDate());
                return map;
            }).collect(Collectors.toList());
        } else {
            List<Expense> expenses = expenseRepository.findByCategoryNameAndStatusAndDateBetweenOrderByDateDesc(
                    category, ExpenseStatus.APPROVED, startDate, endDate);
            response = expenses.stream().map(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", e.getId());
                map.put("description", e.getDescription());
                map.put("amount", e.getAmount());
                map.put("date", e.getDate());
                return map;
            }).collect(Collectors.toList());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan-pool-breakdown")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<LoanService.LoanSummary>> getLoanPoolBreakdown() {
        return ResponseEntity.ok(loanService.getAllLoanSummaries());
    }

    @GetMapping("/capital-pool-breakdown")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCapitalPoolBreakdown() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalIntroduced", capitalService.getTotalApprovedIntroduced());
        map.put("totalDrawings", capitalService.getTotalApprovedDrawings());
        map.put("netCapitalPool", capitalService.getNetCapitalPool());
        return ResponseEntity.ok(map);
    }
}
