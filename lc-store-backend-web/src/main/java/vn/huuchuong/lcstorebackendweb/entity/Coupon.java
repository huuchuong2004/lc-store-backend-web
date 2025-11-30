package vn.huuchuong.lcstorebackendweb.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import vn.huuchuong.lcstorebackendweb.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "coupon",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_coupon_code", columnNames = "coupon_code")
        }
)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer couponId;

    @Column(name = "coupon_code", nullable = false, length = 100)
    private String couponCode;

    @Column(nullable = false)
    private BigDecimal discountValue;

    private LocalDate startDate;



    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal minimumOrderAmount;

    @Column(nullable = false)
    private Integer maxUsage; // số lần dùng tối đa toàn hệ thống

    @Column(nullable = false)
    private Integer maxUsagePerUser; // tối đa mỗi user

    @Column(nullable = false)
    private Integer currentUsage; // tổng số lần đã dùng
}
