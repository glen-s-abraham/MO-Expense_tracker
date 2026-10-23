package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.service.CapitalService;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
public class CapitalController {

    private final CapitalService capitalService;
    private final UserService userService;

    public CapitalController(CapitalService capitalService, UserService userService) {
        this.capitalService = capitalService;
        this.userService = userService;
    }

    @GetMapping("/capital")
    public String capitalDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            @RequestParam(defaultValue = "INTRODUCED") CapitalTransactionType tab,
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
        model.addAttribute("search", search);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("totalIntroduced", capitalService.getTotalApprovedIntroduced());
        model.addAttribute("totalDrawings", capitalService.getTotalApprovedDrawings());
        model.addAttribute("netCapitalPool", capitalService.getNetCapitalPool());

        model.addAttribute("myDrafts", capitalService.getCapitalTransactions(
                user, tab, List.of(IncomeStatus.DRAFT, IncomeStatus.QUERIES_RAISED),
                search, startDate, endDate, PageRequest.of(draftsPage, pageSize, sort)));
        model.addAttribute("pending", capitalService.getCapitalTransactions(
                filterUser, tab, List.of(IncomeStatus.SUBMITTED),
                search, startDate, endDate, PageRequest.of(pendingPage, pageSize, sort)));
        model.addAttribute("approved", capitalService.getCapitalTransactions(
                null, tab, List.of(IncomeStatus.APPROVED),
                search, startDate, endDate, PageRequest.of(approvedPage, pageSize, sort)));
        model.addAttribute("rejected", capitalService.getCapitalTransactions(
                filterUser, tab, List.of(IncomeStatus.REJECTED),
                search, startDate, endDate, PageRequest.of(rejectedPage, pageSize, sort)));

        return "capital/dashboard";
    }

    @GetMapping("/capital/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String newCapitalForm(
            @RequestParam(defaultValue = "INTRODUCED") CapitalTransactionType type,
            Model model) {
        CapitalTransaction tx = new CapitalTransaction();
        tx.setType(type);
        tx.setDate(LocalDate.now());
        model.addAttribute("capitalTransaction", tx);
        model.addAttribute("types", CapitalTransactionType.values());
        model.addAttribute("paymentModes", PaymentMode.values());
        return "capital/form";
    }

    @PostMapping("/capital")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String saveCapital(
            @ModelAttribute CapitalTransaction capitalTransaction,
            @RequestParam(value = "receiptFiles", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteAttachmentIds", required = false) List<Long> deleteAttachmentIds,
            @RequestParam(value = "deletePrimaryImage", required = false, defaultValue = "false") boolean deletePrimaryImage,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) throws IOException {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        CapitalTransaction txToSave;
        if (capitalTransaction.getId() != null) {
            txToSave = capitalService.findById(capitalTransaction.getId()).orElseThrow();
            txToSave.setType(capitalTransaction.getType());
            txToSave.setOwnerName(capitalTransaction.getOwnerName());
            txToSave.setDescription(capitalTransaction.getDescription());
            txToSave.setAmount(capitalTransaction.getAmount());
            txToSave.setDate(capitalTransaction.getDate());
            txToSave.setPaymentMode(capitalTransaction.getPaymentMode());
            txToSave.setBatchId(capitalTransaction.getBatchId());

            if (txToSave.getStatus() == IncomeStatus.REJECTED) {
                txToSave.setStatus(IncomeStatus.DRAFT);
            }
        } else {
            txToSave = capitalTransaction;
            txToSave.setUser(user);
            txToSave.setStatus(IncomeStatus.DRAFT);
        }

        try {
            CapitalTransaction saved = capitalService.saveCapitalTransaction(
                    txToSave, files, deleteAttachmentIds, deletePrimaryImage);
            return "redirect:/capital?tab=" + saved.getType().name();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("capitalTransaction", txToSave);
            model.addAttribute("types", CapitalTransactionType.values());
            model.addAttribute("paymentModes", PaymentMode.values());
            return "capital/form";
        }
    }

    @GetMapping("/capital/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String editCapitalForm(@PathVariable Long id, Model model) {
        CapitalTransaction tx = capitalService.findById(id).orElseThrow();
        model.addAttribute("capitalTransaction", tx);
        model.addAttribute("types", CapitalTransactionType.values());
        model.addAttribute("paymentModes", PaymentMode.values());
        return "capital/form";
    }

    @PostMapping("/capital/submit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String submitCapital(@PathVariable Long id) {
        CapitalTransaction tx = capitalService.updateStatus(id, IncomeStatus.SUBMITTED);
        return "redirect:/capital?tab=" + tx.getType().name();
    }

    @GetMapping("/capital/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String deleteCapital(@PathVariable Long id) {
        CapitalTransactionType type = capitalService.findById(id)
                .map(CapitalTransaction::getType)
                .orElse(CapitalTransactionType.INTRODUCED);
        capitalService.deleteCapitalTransaction(id);
        return "redirect:/capital?tab=" + type.name();
    }

    @DeleteMapping("/capital/attachment/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    @ResponseBody
    public String deleteAttachment(@PathVariable Long id) {
        capitalService.deleteAttachment(id);
        return "";
    }

    @PostMapping("/capital/approve/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String approveCapital(
            @PathVariable Long id,
            @RequestParam(value = "redirectTo", defaultValue = "/approvals") String redirectTo) {
        capitalService.updateStatus(id, IncomeStatus.APPROVED);
        return "redirect:" + redirectTo;
    }

    @PostMapping("/capital/reject/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String rejectCapital(
            @PathVariable Long id,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "redirectTo", defaultValue = "/approvals") String redirectTo,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        if (message != null && !message.trim().isEmpty()) {
            capitalService.addComment(id, user, message);
        }
        capitalService.updateStatus(id, IncomeStatus.REJECTED);
        return "redirect:" + redirectTo;
    }

    @PostMapping("/capital/query/{id}")
    @PreAuthorize("hasAnyRole('ACCOUNTANT', 'SUPERVISOR', 'ADMIN')")
    public String queryCapital(
            @PathVariable Long id,
            @RequestParam("message") String message,
            @RequestParam(value = "redirectTo", defaultValue = "/approvals") String redirectTo,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        capitalService.addComment(id, user, message);
        return "redirect:" + redirectTo;
    }

    @GetMapping("/capital/view/{id}")
    public String viewCapital(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        CapitalTransaction tx = capitalService.findById(id).orElseThrow();
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("capitalTransaction", tx);
        model.addAttribute("comments", capitalService.getComments(id));
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("isOwner", tx.getUser().getId().equals(user.getId()));
        return "capital/view";
    }

    @GetMapping("/capital/export")
    public void exportCapital(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) CapitalTransactionType tab,
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
        Page<CapitalTransaction> page = capitalService.getCapitalTransactions(
                filterUser, tab, List.of(IncomeStatus.values()), search, startDate, endDate, pageable);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"capital_transactions.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Date,Type,Owner/Partner,Amount,PaymentMode,Status,Description,User");
            for (CapitalTransaction tx : page.getContent()) {
                writer.printf("%d,%s,%s,\"%s\",%.2f,%s,%s,\"%s\",%s%n",
                        tx.getId(),
                        tx.getDate(),
                        tx.getType().getDisplayName(),
                        tx.getOwnerName() != null ? tx.getOwnerName().replace("\"", "\"\"") : "",
                        tx.getAmount(),
                        tx.getPaymentMode(),
                        tx.getStatus(),
                        tx.getDescription() != null ? tx.getDescription().replace("\"", "\"\"") : "",
                        tx.getUser().getUsername());
            }
        }
    }
}
