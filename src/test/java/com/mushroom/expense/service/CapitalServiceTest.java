package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CapitalServiceTest {

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void drawingsAdjustAgainstNetCapitalPool() throws IOException {
        double initialIntroduced = capitalService.getTotalApprovedIntroduced();
        double initialNet = capitalService.getNetCapitalPool();
        User admin = userRepository.findByUsername("admin").orElseThrow();

        CapitalTransaction drawing = new CapitalTransaction();
        drawing.setType(CapitalTransactionType.DRAWINGS);
        drawing.setOwnerName("Partner A");
        drawing.setDescription("Owner personal withdrawal");
        drawing.setAmount(15000.0);
        drawing.setDate(LocalDate.now());
        drawing.setPaymentMode(PaymentMode.BANK_TRANSFER);
        drawing.setStatus(IncomeStatus.DRAFT);
        drawing.setUser(admin);

        CapitalTransaction saved = capitalService.saveCapitalTransaction(drawing, List.of(), null, false);
        assertNotNull(saved.getId());

        // While DRAFT, Net Capital does not change
        assertEquals(initialNet, capitalService.getNetCapitalPool(), 0.01);

        // Approve the drawing -> Net Capital decreases by ₹15,000.00
        capitalService.updateStatus(saved.getId(), IncomeStatus.APPROVED);
        assertEquals(15000.0, capitalService.getTotalApprovedDrawings(), 0.01);
        assertEquals(initialIntroduced - 15000.0, capitalService.getNetCapitalPool(), 0.01);

        // Adding a comment sets status to QUERIES_RAISED
        capitalService.addComment(saved.getId(), admin, "Verify voucher");
        assertEquals(IncomeStatus.QUERIES_RAISED, capitalService.findById(saved.getId()).orElseThrow().getStatus());
        assertEquals(1, capitalService.getComments(saved.getId()).size());
    }
}
