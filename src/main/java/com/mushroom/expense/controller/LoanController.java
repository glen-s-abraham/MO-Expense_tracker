package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.service.LoanService;
import com.mushroom.expense.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;

    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @GetMapping("/loans")
    public String loanDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            @RequestParam(defaultValue = "BORROWED") LoanTransactionType tab,
            @RequestParam(required = false) Long loanId,
            @RequestParam(defaultValue = "0") int draftsPage,
            @RequestParam(defaultValue = "0") int pendingPage,
            @RequestParam(defaultValue = "0") int approvedPage,
            @RequestParam(defaultValue = "0") int rejectedPage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        boolean isReviewer = user.getRole().equals("ROLE_ADMIN")
                || user.getRole().equals("ROLE_ACCOUNTANT")
                || user.getRole().equals("ROLE_SUPERVISOR");
        User filterUser = isReviewer ? null : user;

        int pageSize = 8;
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        model.addAttribute("activeType", tab);
        model.addAttribute("selectedLoanId", loanId);
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("loanSummaries", loanService.getAllLoanSummaries());
        model.addAttribute("totalBorrowed", loanService.getTotalApprovedBorrowed());
        model.addAttribute("totalRepaid", loanService.getTotalApprovedRepaid());
        model.addAttribute("outstandingLoanPool", loanService.getOutstandingLoanPool());

        model.addAttribute("myDrafts", loanService.getLoanTransactions(
                user, tab, loanId, List.of(IncomeStatus.DRAFT, IncomeStatus.QUERIES_RAISED),
                search, startDate, endDate, PageRequest.of(draftsPage, pageSize, sort)));
        model.addAttribute("pending", loanService.getLoanTransactions(
                filterUser, tab, loanId, List.of(IncomeStatus.SUBMITTED),
                search, startDate, endDate, PageRequest.of(pendingPage, pageSize, sort)));
        model.addAttribute("approved", loanService.getLoanTransactions(
                null, tab, loanId, List.of(IncomeStatus.APPROVED),
                search, startDate, endDate, PageRequest.of(approvedPage, pageSize, sort)));
        model.addAttribute("rejected", loanService.getLoanTransactions(
                filterUser, tab, loanId, List.of(IncomeStatus.REJECTED),
                search, startDate, endDate, PageRequest.of(rejectedPage, pageSize, sort)));

        return "loan/dashboard";
    }

    @PostMapping("/loans/facility")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String createLoanFacility(
            @RequestParam("loanName") String loanName,
            @RequestParam("lenderName") String lenderName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            RedirectAttributes redirectAttributes) {
        Loan loan = loanService.createOrGetLoanFacility(loanName, lenderName, description, startDate);
        redirectAttributes.addFlashAttribute("successMessage", "Loan facility '" + loan.getLoanName() + "' is ready.");
        return "redirect:/loans";
    }

    @GetMapping("/loans/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String newLoanTransactionForm(
            @RequestParam(defaultValue = "BORROWED") LoanTransactionType type,
            @RequestParam(required = false) Long loanId,
            Model model) {
        LoanTransaction tx = new LoanTransaction();
        tx.setType(type);
        tx.setDate(LocalDate.now());
        if (loanId != null) {
            loanService.findLoanById(loanId).ifPresent(tx::setLoan);
        }
        model.addAttribute("loanTransaction", tx);
        model.addAttribute("loans", loanService.findAllLoans());
        model.addAttribute("loanSummaries", loanService.getAllLoanSummaries());
        model.addAttribute("types", LoanTransactionType.values());
        model.addAttribute("paymentModes", PaymentMode.values());
        return "loan/form";
    }

    @PostMapping("/loans")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String saveLoanTransaction(
            @ModelAttribute LoanTransaction loanTransaction,
            @RequestParam(value = "newLoanName", required = false) String newLoanName,
            @RequestParam(value = "newLenderName", required = false) String newLenderName,
            @RequestParam(value = "receiptFiles", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteAttachmentIds", required = false) List<Long> deleteAttachmentIds,
            @RequestParam(value = "deletePrimaryImage", required = false, defaultValue = "false") boolean deletePrimaryImage,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) throws IOException {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        try {
            if ((loanTransaction.getLoan() == null || loanTransaction.getLoan().getId() == null)
                    && newLoanName != null && !newLoanName.isBlank()) {
                Loan createdLoan = loanService.createOrGetLoanFacility(
                        newLoanName, newLenderName, loanTransaction.getDescription(), loanTransaction.getDate());
                loanTransaction.setLoan(createdLoan);
            }

            LoanTransaction txToSave;
            if (loanTransaction.getId() != null) {
                txToSave = loanService.findById(loanTransaction.getId()).orElseThrow();
                txToSave.setLoan(loanTransaction.getLoan());
                txToSave.setType(loanTransaction.getType());
                txToSave.setDescription(loanTransaction.getDescription());
                txToSave.setAmount(loanTransaction.getAmount());
                txToSave.setDate(loanTransaction.getDate());
                txToSave.setPaymentMode(loanTransaction.getPaymentMode());
                txToSave.setBatchId(loanTransaction.getBatchId());

                if (txToSave.getStatus() == IncomeStatus.REJECTED) {
                    txToSave.setStatus(IncomeStatus.DRAFT);
                }
            } else {
                txToSave = loanTransaction;
                txToSave.setUser(user);
                txToSave.setStatus(IncomeStatus.DRAFT);
            }

            LoanTransaction saved = loanService.saveLoanTransaction(
                    txToSave, files, deleteAttachmentIds, deletePrimaryImage);
            return "redirect:/loans?tab=" + saved.getType().name();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("loanTransaction", loanTransaction);
            model.addAttribute("loans", loanService.findAllLoans());
            model.addAttribute("loanSummaries", loanService.getAllLoanSummaries());
            model.addAttribute("types", LoanTransactionType.values());
            model.addAttribute("paymentModes", PaymentMode.values());
            return "loan/form";
        }
    }

    @GetMapping("/loans/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String editLoanTransactionForm(@PathVariable Long id, Model model) {
        LoanTransaction tx = loanService.findById(id).orElseThrow();
        model.addAttribute("loanTransaction", tx);
        model.addAttribute("loans", loanService.findAllLoans());
        model.addAttribute("loanSummaries", loanService.getAllLoanSummaries());
        model.addAttribute("types", LoanTransactionType.values());
        model.addAttribute("paymentModes", PaymentMode.values());
        return "loan/form";
    }

    @PostMapping("/loans/submit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String submitLoanTransaction(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            LoanTransaction tx = loanService.updateStatus(id, IncomeStatus.SUBMITTED);
            return "redirect:/loans?tab=" + tx.getType().name();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/loans";
        }
    }

    @GetMapping("/loans/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String deleteLoanTransaction(@PathVariable Long id) {
        LoanTransactionType type = loanService.findById(id)
                .map(LoanTransaction::getType)
                .orElse(LoanTransactionType.BORROWED);
        loanService.deleteLoanTransaction(id);
        return "redirect:/loans?tab=" + type.name();
    }

    @DeleteMapping("/loans/attachment/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    @ResponseBody
    public String deleteAttachment(@PathVariable Long id) {
        loanService.deleteAttachment(id);
        return "";
    }

    @PostMapping("/loans/approve/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String approveLoanTransaction(
            @PathVariable Long id,
            @RequestParam(value = "redirectTo", defaultValue = "/approvals") String redirectTo,
            RedirectAttributes redirectAttributes) {
        try {
            loanService.updateStatus(id, IncomeStatus.APPROVED);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/loans/reject/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String rejectLoanTransaction(
            @PathVariable Long id,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "redirectTo", defaultValue = "/approvals") String redirectTo,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        if (message != null && !message.trim().isEmpty()) {
            loanService.addComment(id, user, message);
        }
        loanService.updateStatus(id, IncomeStatus.REJECTED);
        return "redirect:" + redirectTo;
    }

    @PostMapping("/loans/query/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String queryLoanTransaction(
            @PathVariable Long id,
            @RequestParam("message") String message,
            @RequestParam(value = "redirectTo", defaultValue = "/approvals") String redirectTo,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        loanService.addComment(id, user, message);
        return "redirect:" + redirectTo;
    }

    @GetMapping("/loans/view/{id}")
    public String viewLoanTransaction(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        LoanTransaction tx = loanService.findById(id).orElseThrow();
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("loanTransaction", tx);
        model.addAttribute("loanOutstanding", loanService.getLoanOutstandingBalance(tx.getLoan().getId()));
        model.addAttribute("comments", loanService.getComments(id));
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("isOwner", tx.getUser().getId().equals(user.getId()));
        return "loan/view";
    }

    @GetMapping("/loans/export")
    public void exportLoanTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) LoanTransactionType tab,
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDir,
            HttpServletResponse response) throws IOException {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);

        User filterUser = user.getRole().equals("ROLE_MANAGER") ? user : null;
        Page<LoanTransaction> page = loanService.getLoanTransactions(
                filterUser, tab, loanId, List.of(IncomeStatus.values()), search, startDate, endDate, pageable);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"loan_transactions.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Date,LoanFacility,Lender,Type,Amount,PaymentMode,Status,Description,User");
            for (LoanTransaction tx : page.getContent()) {
                writer.printf("%d,%s,\"%s\",\"%s\",%s,%.2f,%s,%s,\"%s\",%s%n",
                        tx.getId(),
                        tx.getDate(),
                        tx.getLoan().getLoanName().replace("\"", "\"\""),
                        tx.getLoan().getLenderName().replace("\"", "\"\""),
                        tx.getType().getDisplayName(),
                        tx.getAmount(),
                        tx.getPaymentMode(),
                        tx.getStatus(),
                        tx.getDescription() != null ? tx.getDescription().replace("\"", "\"\"") : "",
                        tx.getUser().getUsername());
            }
        }
    }
}
