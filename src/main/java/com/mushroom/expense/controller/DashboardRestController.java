package com.mushroom.expense.controller;

import com.mushroom.expense.entity.Expense;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.Income;
import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.repository.ExpenseRepository;
import com.mushroom.expense.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

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
}
