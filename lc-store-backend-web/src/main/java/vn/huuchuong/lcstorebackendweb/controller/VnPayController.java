package vn.huuchuong.lcstorebackendweb.controller;



import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;

import vn.huuchuong.lcstorebackendweb.payload.request.CreateVnPayPaymentRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.CreateVnPayPaymentResponse;
import vn.huuchuong.lcstorebackendweb.service.impl.PaymentServiceImpl;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final PaymentServiceImpl paymentService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<CreateVnPayPaymentResponse>> create(
            @RequestBody CreateVnPayPaymentRequest req,
            HttpServletRequest httpReq) {

        CreateVnPayPaymentResponse res = paymentService.createVnPayPayment(req, httpReq);
        return ResponseEntity.ok(BaseResponse.success(res, "Tạo link thanh toán VNPay thành công"));
    }

    @GetMapping("/return")
    public ResponseEntity<BaseResponse<String>> vnPayReturn(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });

        String resCode = paymentService.handleVnPayReturn(params);
        return ResponseEntity.ok(BaseResponse.success(resCode, "Xử lý callback VNPay thành công"));
    }

    @GetMapping("/ipn")
    public Map<String, String> vnPayIpn(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> {
            if (v != null && v.length > 0) {
                params.put(k, v[0]);
            }
        });

        // Theo spec của VNPay, IPN thường không cần bọc BaseResponse
        // mà trả raw JSON gồm RspCode và Message
        return paymentService.handleVnPayIpn(params);
    }

}

