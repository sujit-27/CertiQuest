package com.web.CertiQuest.controller;

import com.web.CertiQuest.dto.PaymentDto;
import com.web.CertiQuest.dto.PaymentVerificationDto;
import com.web.CertiQuest.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentDto paymentDto){
        PaymentDto response = paymentService.createOrder(paymentDto);

        if(response.getSuccess()){
            return ResponseEntity.ok(response);
        }
        else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDto request) throws Exception {
        PaymentDto response = paymentService.verifyPayment(request);

        if(response.getSuccess()){
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
