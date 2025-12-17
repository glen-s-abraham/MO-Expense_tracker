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
    public String newExpenseForm(Model model) {
        model.addAttribute("expense", new Expense());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("paymentModes", PaymentMode.values());
        return "expense_form";
    }

    @PostMapping("/expense")
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
    public String submitExpense(@PathVariable Long id) {
        expenseService.updateExpenseStatus(id, ExpenseStatus.SUBMITTED);
        return "redirect:/dashboard";
    }

    @GetMapping("/expense/edit/{id}")
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
    public String deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return "redirect:/dashboard";
    }

    // --- Accountant Actions ---

    @PostMapping("/expense/approve/{id}")
    public String approveExpense(@PathVariable Long id) {
        expenseService.updateExpenseStatus(id, ExpenseStatus.APPROVED);
        return "redirect:/dashboard";
    }

    @PostMapping("/expense/reject/{id}")
    public String rejectExpense(@PathVariable Long id,
            @RequestParam(value = "message", required = false) String message,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        if (message != null && !message.trim().isEmpty()) {
            expenseService.addComment(id, user, message);
        }
        expenseService.updateExpenseStatus(id, ExpenseStatus.REJECTED);
        return "redirect:/dashboard";
    }

    @PostMapping("/expense/query/{id}")
    public String queryExpense(@PathVariable Long id,
            @RequestParam("message") String message,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        expenseService.addComment(id, user, message);
        return "redirect:/dashboard";
    }

    @GetMapping("/expense/view/{id}")
    public String viewExpense(@PathVariable Long id, Model model) {
        Expense expense = expenseService.findById(id).orElseThrow();
        model.addAttribute("expense", expense);
        model.addAttribute("comments", expenseService.getComments(id));
        return "expense_view";
    }
}
