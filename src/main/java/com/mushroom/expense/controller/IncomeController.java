package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.service.CategoryService;
import com.mushroom.expense.service.IncomeService;
import com.mushroom.expense.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class IncomeController {

    private final IncomeService incomeService;
    private final CategoryService categoryService;
    private final UserService userService;

    public IncomeController(IncomeService incomeService, CategoryService categoryService, UserService userService) {
        this.incomeService = incomeService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/my-incomes")
    public String myIncomes(@AuthenticationPrincipal UserDetails userDetails, Model model,
            @RequestParam(defaultValue = "0") int draftsPage,
            @RequestParam(defaultValue = "0") int pendingPage,
            @RequestParam(defaultValue = "0") int approvedPage,
            @RequestParam(defaultValue = "0") int returnedPage,
            @RequestParam(defaultValue = "0") int rejectedPage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        int pageSize = 5;
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("categories", categoryService.findCategoriesByType(com.mushroom.expense.entity.TransactionType.INCOME));

        model.addAttribute("myDrafts",
                incomeService.getIncomes(user, List.of(IncomeStatus.DRAFT, IncomeStatus.QUERIES_RAISED), search,
                        startDate, endDate, categoryId,
                        PageRequest.of(draftsPage, pageSize, sort)));
        model.addAttribute("pending",
                incomeService.getIncomes(user, List.of(IncomeStatus.SUBMITTED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(pendingPage, pageSize, sort)));
        model.addAttribute("approved",
                incomeService.getIncomes(user, List.of(IncomeStatus.APPROVED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(approvedPage, pageSize, sort)));
        model.addAttribute("returned",
                incomeService.getIncomes(user, List.of(IncomeStatus.QUERIES_RAISED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(returnedPage, pageSize, sort)));
        model.addAttribute("rejected",
                incomeService.getIncomes(user, List.of(IncomeStatus.REJECTED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(rejectedPage, pageSize, sort)));
        return "income_dashboard";
    }

    @GetMapping("/admin/income-approvals")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String adminIncomeApprovals(@AuthenticationPrincipal UserDetails userDetails, Model model,
            @RequestParam(defaultValue = "0") int submittedPage,
            @RequestParam(defaultValue = "0") int approvedPage,
            @RequestParam(defaultValue = "0") int rejectedPage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        int pageSize = 5;
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("categories", categoryService.findCategoriesByType(com.mushroom.expense.entity.TransactionType.INCOME));

        model.addAttribute("submittedIncomes",
                incomeService.getIncomes(null, List.of(IncomeStatus.SUBMITTED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(submittedPage, pageSize, sort)));
        model.addAttribute("approvedIncomes",
                incomeService.getIncomes(null, List.of(IncomeStatus.APPROVED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(approvedPage, pageSize, sort)));
        model.addAttribute("rejectedIncomes",
                incomeService.getIncomes(null, List.of(IncomeStatus.REJECTED), search, startDate, endDate,
                        categoryId,
                        PageRequest.of(rejectedPage, pageSize, sort)));
        return "income_approvals";
    }

    // --- Actions ---

    @GetMapping("/income/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String newIncomeForm(Model model) {
        Income income = new Income();
        income.setDate(LocalDate.now());
        model.addAttribute("income", income);
        model.addAttribute("categories", categoryService.findCategoriesByType(com.mushroom.expense.entity.TransactionType.INCOME));
        model.addAttribute("paymentModes", PaymentMode.values());
        return "income_form";
    }

    @PostMapping("/income")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String saveIncome(@ModelAttribute Income income,
            @RequestParam("receiptFiles") List<MultipartFile> files,
            @RequestParam(value = "deleteAttachmentIds", required = false) List<Long> deleteAttachmentIds,
            @RequestParam(value = "deletePrimaryImage", required = false, defaultValue = "false") boolean deletePrimaryImage,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        Income incomeToSave;
        if (income.getId() != null) {
            incomeToSave = incomeService.findById(income.getId()).orElseThrow();
            incomeToSave.setCategory(income.getCategory());
            incomeToSave.setDescription(income.getDescription());
            incomeToSave.setAmount(income.getAmount());
            incomeToSave.setDate(income.getDate());
            incomeToSave.setPaymentMode(income.getPaymentMode());
            incomeToSave.setTaxPercentage(income.getTaxPercentage());
            incomeToSave.setBatchId(income.getBatchId());

            if (incomeToSave.getStatus() == IncomeStatus.REJECTED) {
                incomeToSave.setStatus(IncomeStatus.DRAFT);
            }
        } else {
            incomeToSave = income;
            incomeToSave.setUser(user);
            incomeToSave.setStatus(IncomeStatus.DRAFT);
        }

        incomeService.saveIncome(incomeToSave, files, deleteAttachmentIds, deletePrimaryImage);
        return "redirect:/my-incomes";
    }

    @PostMapping("/income/submit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String submitIncome(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        incomeService.updateIncomeStatus(id, IncomeStatus.SUBMITTED);
        return buildMyIncomesRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @GetMapping("/income/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String editIncomeForm(@PathVariable Long id, Model model) {
        Income income = incomeService.findById(id).orElseThrow();
        model.addAttribute("income", income);
        model.addAttribute("categories", categoryService.findCategoriesByType(com.mushroom.expense.entity.TransactionType.INCOME));
        model.addAttribute("paymentModes", PaymentMode.values());
        return "income_form";
    }

    @GetMapping("/income/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String deleteIncome(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        incomeService.deleteIncome(id);
        return buildMyIncomesRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @DeleteMapping("/income/attachment/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    @ResponseBody
    public String deleteAttachment(@PathVariable Long id) {
        incomeService.deleteAttachment(id);
        return ""; 
    }

    // --- Accountant Actions ---

    @PostMapping("/income/approve/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR')")
    public String approveIncome(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        incomeService.updateIncomeStatus(id, IncomeStatus.APPROVED);
        return buildAdminApprovalsRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @PostMapping("/income/reject/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR')")
    public String rejectIncome(@PathVariable Long id,
            @RequestParam(value = "message", required = false) String message,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        if (message != null && !message.trim().isEmpty()) {
            incomeService.addComment(id, user, message);
        }
        incomeService.updateIncomeStatus(id, IncomeStatus.REJECTED);
        return buildAdminApprovalsRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @PostMapping("/income/query/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR')")
    public String queryIncome(@PathVariable Long id,
            @RequestParam("message") String message,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        incomeService.addComment(id, user, message);
        return buildAdminApprovalsRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    private String buildAdminApprovalsRedirectUrl(String search, LocalDate startDate, LocalDate endDate, Long categoryId,
            String sortField, String sortDir) {
        StringBuilder url = new StringBuilder("redirect:/approvals?");
        if (search != null && !search.isEmpty())
            url.append("search=").append(search).append("&");
        if (startDate != null)
            url.append("startDate=").append(startDate).append("&");
        if (endDate != null)
            url.append("endDate=").append(endDate).append("&");
        if (categoryId != null)
            url.append("categoryId=").append(categoryId).append("&");
        url.append("sortField=").append(sortField).append("&");
        url.append("sortDir=").append(sortDir);
        return url.toString();
    }

    private String buildMyIncomesRedirectUrl(String search, LocalDate startDate, LocalDate endDate, Long categoryId,
            String sortField, String sortDir) {
        StringBuilder url = new StringBuilder("redirect:/my-incomes?");
        if (search != null && !search.isEmpty())
            url.append("search=").append(search).append("&");
        if (startDate != null)
            url.append("startDate=").append(startDate).append("&");
        if (endDate != null)
            url.append("endDate=").append(endDate).append("&");
        if (categoryId != null)
            url.append("categoryId=").append(categoryId).append("&");
        url.append("sortField=").append(sortField).append("&");
        url.append("sortDir=").append(sortDir);
        return url.toString();
    }

    @GetMapping("/income/view/{id}")
    public String viewIncome(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Income income = incomeService.findById(id).orElseThrow();
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        model.addAttribute("income", income);
        model.addAttribute("comments", incomeService.getComments(id));
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("isOwner", income.getUser().getId().equals(user.getId()));
        return "income_view";
    }

    @GetMapping("/income/export")
    public void exportIncomes(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        String role = user.getRole();
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);

        List<Income> incomes;

        if (role.equals("ROLE_MANAGER")) {
            Page<Income> page = incomeService.getIncomes(user, List.of(IncomeStatus.values()), search, startDate,
                    endDate, categoryId, pageable);
            incomes = page.getContent();
        } else if (role.equals("ROLE_ACCOUNTANT") || role.equals("ROLE_SUPERVISOR") || role.equals("ROLE_ADMIN")) {
            Page<Income> page = incomeService.getIncomes(null,
                    List.of(IncomeStatus.SUBMITTED, IncomeStatus.APPROVED, IncomeStatus.REJECTED), search, startDate,
                    endDate, categoryId, pageable);
            incomes = page.getContent();
        } else {
            incomes = List.of();
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"incomes.csv\"");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("ID,Date,Category,Amount,Status,Description,User");
            for (Income income : incomes) {
                writer.printf("%d,%s,%s,%.2f,%s,\"%s\",%s%n",
                        income.getId(),
                        income.getDate(),
                        income.getCategory().getName(),
                        income.getAmount(),
                        income.getStatus(),
                        income.getDescription() != null ? income.getDescription().replace("\"", "\"\"") : "",
                        income.getUser().getUsername());
            }
        }
    }
}
