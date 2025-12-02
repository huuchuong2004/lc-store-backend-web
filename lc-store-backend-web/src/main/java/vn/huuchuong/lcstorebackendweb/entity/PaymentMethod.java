package vn.huuchuong.lcstorebackendweb.entity;



import jakarta.persistence.*;
import lombok.*;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentMethodType;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_method_id")
    private Integer paymentMethodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 20, unique = true)
    private PaymentMethodType code;   // COD, VNPAY

    @Column(name = "name", nullable = false, length = 100)
    private String name; // "Thanh toán khi nhận hàng", "Thanh toán VNPay"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}

