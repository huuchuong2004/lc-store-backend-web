package vn.huuchuong.lcstorebackendweb.service.impl;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.entity.OrderItem;
import vn.huuchuong.lcstorebackendweb.repository.IOrderRepository;
import vn.huuchuong.lcstorebackendweb.service.IMailSenderService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentStatus.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailSenderService implements IMailSenderService {

    private final JavaMailSender emailSender;
    private final IOrderRepository orderRepository;
    private final JavaMailSenderImpl mailSender;

    // From lấy từ spring.mail.username
    @Value("${spring.mail.username}")
    private String from;


    private BaseResponse<String> doSendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            // true = multipart, UTF-8 để hiển thị tiếng Việt
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(new InternetAddress(from,"LC Store 💜"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = nội dung là HTML

            emailSender.send(message);

            String msg = "Đã gửi email tới: " + to;
            log.info(msg);
            return BaseResponse.success(msg, "Gửi email thành công");
        } catch (Exception e) {
            String errorMsg = "Gửi email tới " + to + " thất bại: " + e.getMessage();
            log.error(errorMsg, e);
            return BaseResponse.error(errorMsg);
        }
    }

    @Override
    public BaseResponse<String> sendMessageWithAttachment(String to, String subject, String text) {
        // Ở đây text đã là HTML, dùng chung hàm doSendHtmlMail
        return doSendHtmlMail(to, subject, text);
    }

    @Override
    public BaseResponse<String> sendActivationEmail(String to, String activationLink) {
        String subject = "Kích hoạt tài khoản LC-Store";

        String html = """
  <div style="background:#3a3243;padding:24px 0;font-family:Segoe UI,Tahoma,Geneva,Verdana,sans-serif;">
    <div style="background:#594d65;border-radius:16px;
                max-width:420px;margin:0 auto;padding:32px 24px;
                text-align:center;border:1px solid #6b5d7a;
                box-shadow:0 4px 16px rgba(0,0,0,0.3);">
      
      <img src="https://i.ibb.co/8LfXbPdp/z7116621240566-b8a81aef05e5d43c0d6ee6c265d4dbf9.jpg"
           alt="LC Store"
           style="width:120px;border-radius:12px;border:1px solid #827193;margin-bottom:16px;" />
      
      <h2 style="color:#f8f8fa;font-size:20px;margin:0 0 12px 0;">
        Chào mừng bạn đến với LC-Store 💜
      </h2>
      
      <p style="color:#e8e6ee;font-size:14px;line-height:1.6;margin:0 0 24px 0;">
        Bạn đã đăng ký tài khoản thành công.<br/>
        Để kích hoạt tài khoản, vui lòng nhấn vào nút bên dưới.
      </p>
      
      <a href="{{activationLink}}" target="_blank"
         style="display:inline-block;padding:12px 24px;
                background:#a69ab8;color:#ffffff;text-decoration:none;
                border-radius:999px;font-weight:600;font-size:13px;
                letter-spacing:0.05em;text-transform:uppercase;">
        Kích hoạt tài khoản
      </a>
      
      <p style="color:#bfb8cd;font-size:12px;margin-top:20px;">
        Cảm ơn bạn đã tin tưởng LC-Store!
      </p>
    </div>
  </div>
  """.replace("{{activationLink}}", activationLink);




        return doSendHtmlMail(to, subject, html);
    }

    @Transactional
    @Override
    public BaseResponse<String> sendConfirmOrder(String to, Order order) {


        try {
            java.text.NumberFormat formatter =
                    java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));

            String customerName = "Khách hàng";
            if (order.getUser() != null) {
                // sửa lại getter này theo entity User của bạn nếu cần
                if (order.getUser().getUsername() != null && !order.getUser().getUsername().isBlank()) {
                    customerName = order.getUser().getUsername();
                }
            }

            String orderDate = order.getOrderDate() != null
                    ? order.getOrderDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "N/A";

            String shippingAddress = order.getShippingAddress() != null
                    ? order.getShippingAddress()
                    : "Chưa cập nhật";

            String totalAmount = order.getTotalAmount() != null
                    ? formatter.format(order.getTotalAmount())
                    : formatter.format(BigDecimal.ZERO);

            String status = "Đang xử lý";
            if (order.getStatus() != null) {
                switch (order.getStatus()) {

                    case CONFIRMED -> status = "Đã xác nhận";
                    case SHIPPING -> status = "Đang giao";
                    case DELIVERED -> status = "Đã giao";



                    default -> status = order.getStatus().name();
                }
            }

            StringBuilder itemsHtml = new StringBuilder();

            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    String productName = "Sản phẩm";
                    Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                    BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;

                    // Sửa lại getter theo entity ProductVariant/Product nếu cần
                    if (item.getProductVariant() != null
                            && item.getProductVariant().getProduct() != null
                            && item.getProductVariant().getProduct().getName() != null) {
                        productName = item.getProductVariant().getProduct().getName();
                    }

                    BigDecimal lineTotal = unitPrice
                            .multiply(BigDecimal.valueOf(quantity))
                            .subtract(discountAmount);

                    if (lineTotal.compareTo(BigDecimal.ZERO) < 0) {
                        lineTotal = BigDecimal.ZERO;
                    }

                    itemsHtml.append("""
                    <tr>
                        <td style="padding:10px; border:1px solid #ddd;">%s</td>
                        <td style="padding:10px; border:1px solid #ddd; text-align:center;">%d</td>
                        <td style="padding:10px; border:1px solid #ddd; text-align:right;">%s</td>
                        <td style="padding:10px; border:1px solid #ddd; text-align:right;">%s</td>
                    </tr>
                """.formatted(
                            productName,
                            quantity,
                            formatter.format(unitPrice),
                            formatter.format(lineTotal)
                    ));
                }
            } else {
                itemsHtml.append("""
                <tr>
                    <td colspan="4" style="padding:12px; border:1px solid #ddd; text-align:center;">
                        Không có sản phẩm trong đơn hàng
                    </td>
                </tr>
            """);
            }

            String html = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Xác nhận đơn hàng - LC Store</title>
            </head>
            <body style="margin:0; padding:0; background:#f4f4f4; font-family:Arial, sans-serif;">
                <div style="max-width:700px; margin:30px auto; background:#ffffff; border-radius:12px; overflow:hidden; border:1px solid #e5e5e5;">
                    
                    <div style="background:#594d65; color:#ffffff; padding:24px; text-align:center;">
                        <h2 style="margin:0;">Xác nhận đơn hàng - LC Store</h2>
                    </div>

                    <div style="padding:24px; color:#333333;">
                        <p>Xin chào <b>%s</b>,</p>
                        <p>Cảm ơn bạn đã đặt hàng tại <b>LC Store</b>. Đơn hàng của bạn đã được ghi nhận thành công.</p>

                        <div style="background:#f9f9f9; padding:16px; border-radius:8px; margin:20px 0;">
                            <p style="margin:6px 0;"><b>Mã đơn hàng:</b> #%d</p>
                            <p style="margin:6px 0;"><b>Ngày đặt:</b> %s</p>
                            <p style="margin:6px 0;"><b>Trạng thái:</b> %s</p>
                            <p style="margin:6px 0;"><b>Địa chỉ giao hàng:</b> %s</p>
                            <p style="margin:6px 0;"><b>Tổng thanh toán:</b> %s</p>
                        </div>

                        <h3 style="margin-top:24px;">Chi tiết đơn hàng</h3>

                        <table style="width:100%%; border-collapse:collapse; margin-top:12px;">
                            <thead>
                                <tr style="background:#f0f0f0;">
                                    <th style="padding:10px; border:1px solid #ddd; text-align:left;">Sản phẩm</th>
                                    <th style="padding:10px; border:1px solid #ddd; text-align:center;">Số lượng</th>
                                    <th style="padding:10px; border:1px solid #ddd; text-align:right;">Đơn giá</th>
                                    <th style="padding:10px; border:1px solid #ddd; text-align:right;">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>

                        <p style="margin-top:24px;">
                            Chúng tôi sẽ sớm xử lý và giao đơn hàng đến bạn.
                        </p>

                        <p style="margin-top:24px;">Trân trọng,<br><b>LC Store</b></p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(
                    customerName,
                    order.getOrderId(),
                    orderDate,
                    status,
                    shippingAddress,
                    totalAmount,
                    itemsHtml.toString()
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderId() + " - LC Store");
            helper.setText(html, true);

            mailSender.send(message);

            return BaseResponse.success("OK", "Gửi email xác nhận đơn hàng thành công");

        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi email xác nhận đơn hàng: " + e.getMessage(), e);
        }
    }
}
