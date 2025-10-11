package com.web.CertiQuest.controller;

import com.web.CertiQuest.dao.PaymentTransactionDao;
import com.web.CertiQuest.model.PaymentTransaction;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/transactions")
public class TransactionController {

    @Autowired
    private PaymentTransactionDao paymentTransactionDao;
    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getUserTransactions(){
        Profile currentProfile = profileService.getCurrentProfile();
        String clerkId = currentProfile.getClerkId();

        List<PaymentTransaction> transactions = paymentTransactionDao.findByClerkIdAndStatusOrderByTransactionDateDesc(clerkId,"SUCCESS");

        return ResponseEntity.ok(transactions);
    }
}

