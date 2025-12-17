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
            @RequestParam(defaultValue = "0") int submittedPage) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        String role = user.getRole();
        int pageSize = 5;

        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin/users";
        } else if (role.equals("ROLE_MANAGER")) {
            model.addAttribute("myDrafts",
                    expenseService.findExpensesByUserAndStatusIn(user,
                            List.of(ExpenseStatus.DRAFT, ExpenseStatus.QUERIES_RAISED),
                            PageRequest.of(draftsPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("pending", expenseService.findExpensesByUserAndStatus(user, ExpenseStatus.SUBMITTED,
                    PageRequest.of(pendingPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("approved", expenseService.findExpensesByUserAndStatus(user, ExpenseStatus.APPROVED,
                    PageRequest.of(approvedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("returned",
                    expenseService.findExpensesByUserAndStatus(user, ExpenseStatus.QUERIES_RAISED,
                            PageRequest.of(returnedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("rejected", expenseService.findExpensesByUserAndStatus(user, ExpenseStatus.REJECTED,
                    PageRequest.of(rejectedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            return "manager/dashboard";
        } else if (role.equals("ROLE_ACCOUNTANT")) {
            model.addAttribute("submittedExpenses", expenseService.findExpensesByStatus(ExpenseStatus.SUBMITTED,
                    PageRequest.of(submittedPage, pageSize, Sort.by(Sort.Direction.ASC, "date"))));
            model.addAttribute("approvedExpenses", expenseService.findExpensesByStatus(ExpenseStatus.APPROVED,
                    PageRequest.of(approvedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("rejectedExpenses", expenseService.findExpensesByStatus(ExpenseStatus.REJECTED,
                    PageRequest.of(rejectedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            return "accountant/dashboard";
        } else if (role.equals("ROLE_SUPERVISOR")) {
            model.addAttribute("myDrafts",
                    expenseService.findExpensesByUserAndStatusIn(user,
                            List.of(ExpenseStatus.DRAFT, ExpenseStatus.QUERIES_RAISED),
                            PageRequest.of(draftsPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("submittedExpenses", expenseService.findExpensesByStatus(ExpenseStatus.SUBMITTED,
                    PageRequest.of(submittedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("approvedExpenses", expenseService.findExpensesByStatus(ExpenseStatus.APPROVED,
                    PageRequest.of(approvedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
            model.addAttribute("rejectedExpenses", expenseService.findExpensesByStatus(ExpenseStatus.REJECTED,
                    PageRequest.of(rejectedPage, pageSize, Sort.by(Sort.Direction.DESC, "date"))));
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
