package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.service.CategoryService;
import com.mushroom.expense.service.ExpenseService;
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
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final UserService userService;

    public ExpenseController(ExpenseService expenseService, CategoryService categoryService, UserService userService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model,
            @RequestParam(defaultValue = "0") int draftsPage,
            @RequestParam(defaultValue = "0") int pendingPage,
            @RequestParam(defaultValue = "0") int approvedPage,
            @RequestParam(defaultValue = "0") int returnedPage,
            @RequestParam(defaultValue = "0") int rejectedPage,
            @RequestParam(defaultValue = "0") int submittedPage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        String role = user.getRole();
        int pageSize = 5;
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        // Add filter params to model for UI persistence
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("categories", categoryService.findAllCategories()); // For filter dropdown

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/users";
        } else if (role.equals("ROLE_MANAGER")) {
            model.addAttribute("myDrafts",
                    expenseService.getExpenses(user, List.of(ExpenseStatus.DRAFT, ExpenseStatus.QUERIES_RAISED), search,
                            startDate, endDate, categoryId,
                            PageRequest.of(draftsPage, pageSize, sort)));
            model.addAttribute("pending",
                    expenseService.getExpenses(user, List.of(ExpenseStatus.SUBMITTED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(pendingPage, pageSize, sort)));
            model.addAttribute("approved",
                    expenseService.getExpenses(user, List.of(ExpenseStatus.APPROVED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(approvedPage, pageSize, sort)));
            model.addAttribute("returned",
                    expenseService.getExpenses(user, List.of(ExpenseStatus.QUERIES_RAISED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(returnedPage, pageSize, sort)));
            model.addAttribute("rejected",
                    expenseService.getExpenses(user, List.of(ExpenseStatus.REJECTED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(rejectedPage, pageSize, sort)));
            return "manager/dashboard";
        } else if (role.equals("ROLE_ACCOUNTANT")) {
            model.addAttribute("submittedExpenses",
                    expenseService.getExpenses(null, List.of(ExpenseStatus.SUBMITTED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(submittedPage, pageSize, sort)));
            model.addAttribute("approvedExpenses",
                    expenseService.getExpenses(null, List.of(ExpenseStatus.APPROVED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(approvedPage, pageSize, sort)));
            model.addAttribute("rejectedExpenses",
                    expenseService.getExpenses(null, List.of(ExpenseStatus.REJECTED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(rejectedPage, pageSize, sort)));
            return "accountant/dashboard";
        } else if (role.equals("ROLE_SUPERVISOR")) {
            model.addAttribute("myDrafts",
                    expenseService.getExpenses(user, List.of(ExpenseStatus.DRAFT, ExpenseStatus.QUERIES_RAISED), search,
                            startDate, endDate, categoryId,
                            PageRequest.of(draftsPage, pageSize, sort)));
            model.addAttribute("submittedExpenses",
                    expenseService.getExpenses(null, List.of(ExpenseStatus.SUBMITTED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(submittedPage, pageSize, sort)));
            model.addAttribute("approvedExpenses",
                    expenseService.getExpenses(null, List.of(ExpenseStatus.APPROVED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(approvedPage, pageSize, sort)));
            model.addAttribute("rejectedExpenses",
                    expenseService.getExpenses(null, List.of(ExpenseStatus.REJECTED), search, startDate, endDate,
                            categoryId,
                            PageRequest.of(rejectedPage, pageSize, sort)));
            return "supervisor/dashboard";
        }

        return "dashboard"; // Fallback
    }

    // --- Manager Actions ---

    @GetMapping("/expense/new")
    @PreAuthorize("hasRole('MANAGER')")
    public String newExpenseForm(Model model) {
        model.addAttribute("expense", new Expense());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("paymentModes", PaymentMode.values());
        return "expense_form";
    }

    @PostMapping("/expense")
    @PreAuthorize("hasRole('MANAGER')")
    public String saveExpense(@ModelAttribute Expense expense,
            @RequestParam("receiptFiles") List<MultipartFile> files,
            @RequestParam(value = "deleteAttachmentIds", required = false) List<Long> deleteAttachmentIds,
            @RequestParam(value = "deletePrimaryImage", required = false, defaultValue = "false") boolean deletePrimaryImage,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        expense.setUser(user);

        // If it was QUERIES_RAISED, change to SUBMITTED upon edit?
        // Or user explicitly submits? Let's assume saving keeps it DRAFT unless
        // explicitly submitted.
        // But for simplicity, let's say "Save" puts it in DRAFT.
        // We need a "Submit" button logic.
        // For now, let's just save as DRAFT if new, or keep status if editing.
        // Actually, let's force DRAFT if it's new.
        if (expense.getId() == null) {
            expense.setStatus(ExpenseStatus.DRAFT);
        } else {
            // If editing, keep existing status unless we want to reset.
            // But usually editing a DRAFT keeps it DRAFT.
            // Editing a QUERIES_RAISED might keep it QUERIES_RAISED until "Submit" is
            // clicked.
            // Let's handle "Submit" via a separate action or a flag.
        }

        expenseService.saveExpense(expense, files, deleteAttachmentIds, deletePrimaryImage);
        return "redirect:/dashboard";
    }

    @PostMapping("/expense/submit/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String submitExpense(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        expenseService.updateExpenseStatus(id, ExpenseStatus.SUBMITTED);
        return buildRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @GetMapping("/expense/edit/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String editExpenseForm(@PathVariable Long id, Model model) {
        Expense expense = expenseService.findById(id).orElseThrow();
        model.addAttribute("expense", expense);
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("subCategories",
                categoryService.findSubCategoriesByCategoryId(expense.getCategory().getId()));
        model.addAttribute("paymentModes", PaymentMode.values());
        return "expense_form";
    }

    @GetMapping("/expense/delete/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public String deleteExpense(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        expenseService.deleteExpense(id);
        return buildRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    // --- Accountant Actions ---

    @PostMapping("/expense/approve/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR')")
    public String approveExpense(@PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        expenseService.updateExpenseStatus(id, ExpenseStatus.APPROVED);
        return buildRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @PostMapping("/expense/reject/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR')")
    public String rejectExpense(@PathVariable Long id,
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
            expenseService.addComment(id, user, message);
        }
        expenseService.updateExpenseStatus(id, ExpenseStatus.REJECTED);
        return buildRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    @PostMapping("/expense/query/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR')")
    public String queryExpense(@PathVariable Long id,
            @RequestParam("message") String message,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        expenseService.addComment(id, user, message);
        return buildRedirectUrl(search, startDate, endDate, categoryId, sortField, sortDir);
    }

    private String buildRedirectUrl(String search, LocalDate startDate, LocalDate endDate, Long categoryId,
            String sortField, String sortDir) {
        StringBuilder url = new StringBuilder("redirect:/dashboard?");
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

    @GetMapping("/expense/view/{id}")
    public String viewExpense(@PathVariable Long id, Model model) {
        Expense expense = expenseService.findById(id).orElseThrow();
        model.addAttribute("expense", expense);
        model.addAttribute("comments", expenseService.getComments(id));
        return "expense_view";
    }

    @GetMapping("/expense/export")
    public void exportExpenses(@AuthenticationPrincipal UserDetails userDetails,
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

        List<Expense> expenses;

        if (role.equals("ROLE_MANAGER")) {
            Page<Expense> page = expenseService.getExpenses(user, List.of(ExpenseStatus.values()), search, startDate,
                    endDate, categoryId, pageable);
            expenses = page.getContent();
        } else if (role.equals("ROLE_ACCOUNTANT") || role.equals("ROLE_SUPERVISOR")) {
            Page<Expense> page = expenseService.getExpenses(null,
                    List.of(ExpenseStatus.SUBMITTED, ExpenseStatus.APPROVED, ExpenseStatus.REJECTED), search, startDate,
                    endDate, categoryId, pageable);
            expenses = page.getContent();
        } else {
            expenses = List.of();
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"expenses.csv\"");

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.println("ID,Date,Category,SubCategory,Amount,Status,Description,User");
            for (Expense expense : expenses) {
                writer.printf("%d,%s,%s,%s,%.2f,%s,\"%s\",%s%n",
                        expense.getId(),
                        expense.getDate(),
                        expense.getCategory().getName(),
                        expense.getSubCategory().getName(),
                        expense.getAmount(),
                        expense.getStatus(),
                        expense.getDescription() != null ? expense.getDescription().replace("\"", "\"\"") : "",
                        expense.getUser().getUsername());
            }
        }
    }
}
