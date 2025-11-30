package vn.huuchuong.lcstorebackendweb.payload.request.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateCouponRequest {
    private String couponCode;
    private BigDecimal discountValue;


    private LocalDate endDate;
    private BigDecimal minimumOrderAmount;
    private Integer maxUsage;
    private Integer maxUsagePerUser;
}
