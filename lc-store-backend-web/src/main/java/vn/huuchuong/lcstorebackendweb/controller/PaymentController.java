package vn.huuchuong.lcstorebackendweb.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Payment;

import vn.huuchuong.lcstorebackendweb.payload.request.CreateCodPaymentRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.PaymentStatusResponse;
import vn.huuchuong.lcstorebackendweb.service.impl.PaymentServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @PostMapping("/cod")
    public ResponseEntity<BaseResponse<Map<String, Object>>> createCodPayment(
            @RequestBody CreateCodPaymentRequest req) {

        Payment payment = paymentService.createCodPayment(req);

        Map<String, Object> response = new HashMap<>(); // chi lay tt cna thiet
        response.put("paymentId", payment.getPaymentId());
        response.put("amount", payment.getAmount());
        response.put("status", payment.getStatus());
        response.put("orderId", payment.getOrder().getOrderId());

        if (payment.getPaymentMethod() != null) {
            response.put("paymentMethod", payment.getPaymentMethod().getName());
        } else {
            response.put("paymentMethod", "COD");
        }

        return ResponseEntity.ok(BaseResponse.success(response, "Tạo thanh toán COD thành công"));
    }

    @PutMapping("/cod/{paymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> confirmCodPayment(
            @PathVariable Integer paymentId) {

        paymentService.confirmCodPayment(paymentId);
        return ResponseEntity.ok(
                BaseResponse.success(null, "Xác nhận thanh toán COD thành công")
        );
    }

    @GetMapping("/{orderID}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getPaymentMethodByOrderID(
            @PathVariable Integer orderID) {

        Payment payment = paymentService.getPaymentMethodByOrderID(orderID);

        Map<String, Object> paymentInfo = new HashMap<>();
        if (payment != null) {
            paymentInfo.put("paymentId", payment.getPaymentId());

            //
            if (payment.getPaymentMethod() != null) {

                paymentInfo.put("paymentMethod", payment.getPaymentMethod().getName());
            } else {
                paymentInfo.put("paymentMethod", "COD");
            }

            paymentInfo.put("amount", payment.getAmount());
            paymentInfo.put("status", payment.getStatus());
        }

        return ResponseEntity.ok(BaseResponse.success(paymentInfo, "Lấy thông tin thanh toán thành công"));
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<BaseResponse<PaymentStatusResponse>> getPaymentStatusByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        paymentService.getPaymentStatusByOrderId(orderId),
                        "Lấy trạng thái thanh toán thành công"
                )
        );
    }
}
