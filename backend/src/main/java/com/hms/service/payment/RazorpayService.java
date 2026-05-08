package com.hms.service.payment;

import com.hms.exception.BusinessRuleException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay Client: {}", e.getMessage());
        }
    }

    public String createOrder(BigDecimal amount, String receiptId) {
        try {
            JSONObject orderRequest = new JSONObject();
            // Razorpay amount is in paise, so multiply by 100
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receiptId);
            orderRequest.put("payment_capture", 1); // Auto capture

            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: {}", e.getMessage());
            throw new BusinessRuleException("Payment initialization failed");
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
