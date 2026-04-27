package vn.huuchuong.lcstorebackendweb.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.config.VnPayConfig;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.entity.Payment;
import vn.huuchuong.lcstorebackendweb.entity.PaymentMethod;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.OrderStatus;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentMethodType;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentStatus;
import vn.huuchuong.lcstorebackendweb.exception.BusinessException;
import vn.huuchuong.lcstorebackendweb.payload.request.CreateCodPaymentRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.CreateVnPayPaymentRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.CreateVnPayPaymentResponse;
import vn.huuchuong.lcstorebackendweb.payload.response.PaymentStatusResponse;
import vn.huuchuong.lcstorebackendweb.repository.IOrderRepository;
import vn.huuchuong.lcstorebackendweb.repository.IPaymentMethodRepository;
import vn.huuchuong.lcstorebackendweb.repository.IPaymentRepository;
import vn.huuchuong.lcstorebackendweb.service.IInvoiceService;
import vn.huuchuong.lcstorebackendweb.utils.VnPayUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {

    private final IPaymentRepository paymentRepository;
    private final IPaymentMethodRepository paymentMethodRepository;
    private final IOrderRepository orderRepository;
    private final VnPayConfig vnPayConfig;
    private final IInvoiceService invoiceService;
    private final MailSenderService mailSenderService;

    // ====================== VNPay: Tạo payment + link thanh toán ======================

    @Transactional
    public CreateVnPayPaymentResponse createVnPayPayment(CreateVnPayPaymentRequest req,
                                                         HttpServletRequest httpReq) {

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Trạng thái đơn hàng không hợp lệ để tạo thanh toán");
        }

        User user = order.getUser();

        PaymentMethod method = paymentMethodRepository.findByCode(PaymentMethodType.VNPAY)
                .orElseThrow(() -> new BusinessException("Chưa cấu hình phương thức VNPAY"));

        // 🔹 Tạo payment PENDING
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);

        // 🔹 txnRef duy nhất
        String txnRef = "PM" + payment.getPaymentId();
        payment.setTxnRef(txnRef);

        // 🔹 Build param VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());

        long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getOrderId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

        String ipAddress = httpReq.getRemoteAddr();
        vnpParams.put("vnp_IpAddr", ipAddress);


        String createDate = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpParams.put("vnp_CreateDate", createDate);

        // 🔹 sort key để tạo chuỗi hash
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                // SỬA LẠI: Phải Encode giá trị trước khi nối vào chuỗi hash
                try {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String secureHash = VnPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        vnpParams.put("vnp_SecureHash", secureHash);

        String payQuery = VnPayUtils.buildQueryString(vnpParams);
        String payUrl = vnPayConfig.getPayUrl() + "?" + payQuery;

        payment.setPayUrl(payUrl);
        paymentRepository.save(payment);



        return CreateVnPayPaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .payUrl(payUrl)
                .build();
    }

    // ====================== VNPay: Return (trả về cho FE) ======================

    @Transactional
    public String handleVnPayReturn(Map<String, String> allParams) {

        String vnpSecureHash = allParams.get("vnp_SecureHash");
        allParams.remove("vnp_SecureHash");
        allParams.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(allParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = allParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                try {
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    // SỬA LẠI: Encode giá trị trước khi nối vào chuỗi hash
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String checkHash = VnPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        // Debug log để kiểm tra nếu cần
        // System.out.println("My Hash: " + checkHash);
        // System.out.println("VNP Hash: " + vnpSecureHash);

        if (!checkHash.equalsIgnoreCase(vnpSecureHash)) {
            throw new BusinessException("Chữ ký VNPay không hợp lệ");
        }

        String txnRef = allParams.get("vnp_TxnRef");
        String responseCode = allParams.get("vnp_ResponseCode");
        String transactionNo = allParams.get("vnp_TransactionNo");
        String bankCode = allParams.get("vnp_BankCode");
        String bankTranNo = allParams.get("vnp_BankTranNo");

        Payment payment = paymentRepository.findByTxnRef(txnRef)
            .orElseThrow(() -> new BusinessException("Không tìm thấy giao dịch thanh toán"));

        Order order = payment.getOrder();

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException("Đơn hàng đã bị hủy, không thể xác nhận thanh toán");
        }

        // Nếu đã success từ IPN rồi thì chỉ trả responseCode cho FE
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return "00";
        }

        payment.setResponseCode(responseCode);
        payment.setTransactionNo(transactionNo);
        payment.setBankCode(bankCode);
        payment.setBankTranNo(bankTranNo);
        payment.setPaymentDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));


        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            if (order.getStatus() == OrderStatus.CREATED) {
                order.setStatus(OrderStatus.CONFIRMED);
            }

            // Tùy bạn: có thể tạo invoice ở đây hoặc chỉ rely vào IPN
            invoiceService.createInvoiceForOrderIfNotExists(order);

        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return responseCode;
    }

    // ====================== COD: tạo payment ======================

    @Transactional
    public Payment createCodPayment(CreateCodPaymentRequest req) {

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));

        User user = order.getUser();

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Trạng thái đơn hàng không hợp lệ để tạo thanh toán COD");
        }

        PaymentMethod codMethod = paymentMethodRepository.findByCode(PaymentMethodType.COD)
                .orElseThrow(() -> new BusinessException("Chưa cấu hình phương thức COD"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(codMethod);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(null);

        Payment saved = paymentRepository.save(payment);

        // Business: chọn COD xong thì xem như đơn đã được xác nhận
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        BaseResponse<String> mailResult =
                mailSenderService.sendConfirmOrder(user.getEmail(), order);

        String message;
        if (mailResult.getData() == null) {
            message = "Đặt hàng thành công nhưng gửi email xác nhận thất bại: "
                    + mailResult.getMessage();
        } else {
            message = "Đặt hàng thành công! Vui lòng kiểm tra email để xem chi tiết đơn hàng.";
        }


        return saved;
    }

    // ====================== VNPay: IPN (webhook server-to-server) ======================

    @Transactional
    public Map<String, String> handleVnPayIpn(Map<String, String> allParams) {

        String vnpSecureHash = allParams.get("vnp_SecureHash");
        allParams.remove("vnp_SecureHash");
        allParams.remove("vnp_SecureHashType");

        // Build lại chuỗi để verify (có encode giá trị trước khi hash)
        List<String> fieldNames = new ArrayList<>(allParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = allParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (hashData.length() > 0) hashData.append('&');
                try {
                    hashData.append(fieldName)
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String checkHash = VnPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        Map<String, String> res = new HashMap<>();

        if (!checkHash.equalsIgnoreCase(vnpSecureHash)) {
            res.put("RspCode", "97");
            res.put("Message", "Invalid signature");
            return res;
        }

        String txnRef = allParams.get("vnp_TxnRef");
        String responseCode = allParams.get("vnp_ResponseCode");
        String transactionNo = allParams.get("vnp_TransactionNo");
        String bankCode = allParams.get("vnp_BankCode");
        String bankTranNo = allParams.get("vnp_BankTranNo");

        Payment payment = paymentRepository.findByTxnRef(txnRef)
            .orElse(null);

        if (payment == null) {
            res.put("RspCode", "01");
            res.put("Message", "Order not found");
            return res;
        }

        Order order = payment.getOrder();

        // Đơn đã hủy -> từ chối xác nhận thanh toán
        if (order.getStatus() == OrderStatus.CANCELED) {
            res.put("RspCode", "02");
            res.put("Message", "Order was canceled");
            return res;
        }

        // Đã xử lý success trước đó rồi → idempotent
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            res.put("RspCode", "00");
            res.put("Message", "Order already confirmed");
            return res;
        }

        payment.setResponseCode(responseCode);
        payment.setTransactionNo(transactionNo);
        payment.setBankCode(bankCode);
        payment.setBankTranNo(bankTranNo);
        payment.setPaymentDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));


        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            BaseResponse<String> mailResult =
                    mailSenderService.sendConfirmOrder(order.getUser().getEmail(), order);

            String message;
            if (mailResult.getData() == null) {
                message = "Đặt hàng thành công nhưng gửi email xác nhận thất bại: "
                        + mailResult.getMessage();
            } else {
                message = "Đặt hàng thành công! Vui lòng kiểm tra email để xem chi tiết đơn hàng.";
            }





            if (order.getStatus() == OrderStatus.CREATED) {
                order.setStatus(OrderStatus.CONFIRMED);
            }

            paymentRepository.save(payment);
            orderRepository.save(order);

            // ⭐ Tạo hoá đơn (idempotent bên trong service)
            invoiceService.createInvoiceForOrderIfNotExists(order);

            res.put("RspCode", "00");
            res.put("Message", "Success");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            orderRepository.save(order);

            res.put("RspCode", "00");
            res.put("Message", "Payment failed");
        }

        return res;
    }

    // ====================== Admin xác nhận COD đã thu tiền ======================

    @Transactional
    public void confirmCodPayment(Integer paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy payment"));

        if (payment.getPaymentMethod().getCode() != PaymentMethodType.COD) {
            throw new BusinessException("Payment này không phải COD");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BusinessException("Payment COD này đã được xác nhận trước đó");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Trạng thái payment không hợp lệ để xác nhận COD");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));


        Order order = payment.getOrder();
        if (order.getStatus() != OrderStatus.SHIPPING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Chỉ xác nhận COD khi đơn đã được xác nhận hoặc đang giao (CONFIRMED/SHIPPING)");
        }

        // COD: thu tiền xong xem như giao xong
        order.setStatus(OrderStatus.DELIVERED);

        paymentRepository.save(payment);
        orderRepository.save(order);

        // ⭐ Tạo hoá đơn cho COD
        invoiceService.createInvoiceForOrderIfNotExists(order);
    }


    public Payment getPaymentMethodByOrderID(Integer orderID) {
        Order order = orderRepository.findById(orderID).orElse(null);
        if (order == null) return null;

        // Lấy danh sách các lần thanh toán
        List<Payment> payments = paymentRepository.findByOrder(order);

        if (payments == null || payments.isEmpty()) {
            return null;
        }

        // Sắp xếp để lấy cái mới nhất (giả sử ID lớn hơn là mới hơn)
        // Hoặc bạn có thể ưu tiên lấy cái có status = "COMPLETED"
        payments.sort((p1, p2) -> p2.getPaymentId().compareTo(p1.getPaymentId()));

        // Trả về cái mới nhất
        return payments.get(0);
    }

    public PaymentStatusResponse getPaymentStatusByOrderId(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Đơn hàng không tồn tại"));

        List<Payment> payments = paymentRepository.findByOrder(order);

        if (payments == null || payments.isEmpty()) {
            throw new BusinessException("Không tìm thấy thông tin thanh toán cho đơn hàng này");
        }

        // Giả sử lấy payment mới nhất dựa trên paymentId
        payments.sort((p1, p2) -> p2.getPaymentId().compareTo(p1.getPaymentId()));
        Payment latestPayment = payments.get(0);

        return PaymentStatusResponse.builder()
                .orderId(order.getOrderId())
                .paymentId(latestPayment.getPaymentId())
                .paymentStatus(latestPayment.getStatus())
                .amount(latestPayment.getAmount())
                .paymentMethod(
                        latestPayment.getPaymentMethod() != null
                                ? latestPayment.getPaymentMethod().getName()
                                : "COD"
                )
                .build();

    }
}
