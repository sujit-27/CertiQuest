package com.web.CertiQuest.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.web.CertiQuest.dao.PaymentTransactionDao;
import com.web.CertiQuest.dto.PaymentDto;
import com.web.CertiQuest.dto.PaymentVerificationDto;
import com.web.CertiQuest.model.PaymentTransaction;
import com.web.CertiQuest.model.Profile;
import com.web.CertiQuest.model.UserPoints;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserPointsService userPointsService;

    @Autowired
    private PaymentTransactionDao repo;

    @Value("${razorpay.key_id}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret}")
    private String razorpayKeySecret;

    public PaymentDto createOrder(PaymentDto paymentDto) {
        PaymentDto paymentDto1 = new PaymentDto();
        try {
            Profile profile = profileService.getCurrentProfile();
            String clerkId = profile.getClerkId();

            // Handle Free Plan - No Razorpay order needed
            if ("free".equalsIgnoreCase(paymentDto.getPlanId())) {
                PaymentTransaction transaction = new PaymentTransaction();
                transaction.setClerkId(clerkId);
                transaction.setOrderId("FREE_PLAN_" + System.currentTimeMillis());
                transaction.setPlanId("free");
                transaction.setAmount(0);
                transaction.setCurrency("INR");
                transaction.setStatus("SUCCESS");
                transaction.setTransactionDate(LocalDateTime.now());
                transaction.setUserEmail(profile.getEmail());
                transaction.setUserName(profile.getFirstName() + " " + profile.getLastName());
                transaction.setPointsAdded(0); // Free plan gives no points
                repo.save(transaction);

                paymentDto1.setSuccess(true);
                paymentDto1.setMessage("Free plan activated successfully!");
                paymentDto1.setOrderId(transaction.getOrderId());
                return paymentDto1;
            }

            // Paid Plans - Create Razorpay order
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentDto.getAmount()); // Amount in paise expected
            orderRequest.put("currency", paymentDto.getCurrency());
            orderRequest.put("receipt", "order_" + System.currentTimeMillis());

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setClerkId(clerkId);
            transaction.setOrderId(orderId);
            transaction.setPlanId(paymentDto.getPlanId());
            transaction.setAmount(paymentDto.getAmount());
            transaction.setCurrency(paymentDto.getCurrency());
            transaction.setStatus("PENDING");
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setUserEmail(profile.getEmail());
            transaction.setUserName(profile.getFirstName() + " " + profile.getLastName());
            repo.save(transaction);

            paymentDto1.setOrderId(orderId);
            paymentDto1.setSuccess(true);
            paymentDto1.setMessage("Order Created Successfully!");
        } catch (Exception e) {
            logger.error("Error Creating Order", e);
            paymentDto1.setSuccess(false);
            paymentDto1.setMessage("Error Creating Order: " + e.getMessage());
        }
        return paymentDto1;
    }

    private static String generateHmacSha256Signature(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public PaymentDto verifyPayment(PaymentVerificationDto request) {
        PaymentDto paymentDto = new PaymentDto();
        try {
            Profile currentProfile = profileService.getCurrentProfile();
            String clerkId = currentProfile.getClerkId();

            // Skip verification for Free Plan
            if ("free".equalsIgnoreCase(request.getPlanId())) {
                paymentDto.setSuccess(true);
                paymentDto.setMessage("Free plan does not require payment verification.");
                return paymentDto;
            }

            String data = request.getRazorpay_order_id() + "|" + request.getRazorpay_payment_id();
            String generatedSignature = generateHmacSha256Signature(data, razorpayKeySecret);

            if (!generatedSignature.equals(request.getRazorpay_signature())) {
                updateTransactionStatus(request.getRazorpay_order_id(), "FAILED", request.getRazorpay_payment_id(), null);
                paymentDto.setSuccess(false);
                paymentDto.setMessage("Payment Signature Verification failed.");
                return paymentDto;
            }

            // Map plan to points dynamically
            int pointsToAdd = 0;
            String plan = request.getPlanId().toUpperCase();

            switch (request.getPlanId().toLowerCase()) {
                case "premium":
                    pointsToAdd = 500;
                    break;
                case "ultimate":
                    pointsToAdd = 5000;
                    break;
                default:
                    pointsToAdd = 0;
                    break;
            }

            if (pointsToAdd > 0) {
                UserPoints userPoints = userPointsService.getUserPoints(clerkId);
                userPointsService.addPoints(clerkId, pointsToAdd, plan);
                updateTransactionStatus(request.getRazorpay_order_id(), "SUCCESS", request.getRazorpay_payment_id(), pointsToAdd);
                paymentDto.setSuccess(true);
                paymentDto.setMessage(plan + " Plan activated. Points added successfully.");
                paymentDto.setPoints(userPoints != null ? userPoints.getPoints() + pointsToAdd : pointsToAdd);
            } else {
                updateTransactionStatus(request.getRazorpay_order_id(), "FAILED", request.getRazorpay_payment_id(), null);
                paymentDto.setSuccess(false);
                paymentDto.setMessage("Invalid plan selected.");
            }

        } catch (Exception e) {
            logger.error("Error verifying payment", e);
            try {
                updateTransactionStatus(request.getRazorpay_order_id(), "ERROR", request.getRazorpay_payment_id(), null);
            } catch (Exception ex) {
                logger.error("Error updating transaction status", ex);
            }
            paymentDto.setSuccess(false);
            paymentDto.setMessage("Error verifying payment: " + e.getMessage());
        }
        return paymentDto;
    }

    private void updateTransactionStatus(String razorpayOrderId, String status, String razorpayPaymentId, Integer pointsToAdd) {
        Optional<PaymentTransaction> transactionOpt = repo.findByOrderId(razorpayOrderId);
        transactionOpt.ifPresent(transaction -> {
            transaction.setStatus(status);
            transaction.setPaymentId(razorpayPaymentId);
            if (pointsToAdd != null) {
                transaction.setPointsAdded(pointsToAdd);
            }
            repo.save(transaction);
        });
    }
}
