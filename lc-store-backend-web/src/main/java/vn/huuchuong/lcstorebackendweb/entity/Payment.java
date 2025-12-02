package vn.huuchuong.lcstorebackendweb.entity;



import jakarta.persistence.*;
import lombok.*;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // ==== VNPay fields ====
    @Column(name = "txn_ref", length = 50)
    private String txnRef;          // vnp_TxnRef

    @Column(name = "transaction_no", length = 50)
    private String transactionNo;   // vnp_TransactionNo

    @Column(name = "bank_code", length = 50)
    private String bankCode;        // vnp_BankCode

    @Column(name = "bank_tran_no", length = 50)
    private String bankTranNo;      // vnp_BankTranNo

    @Column(name = "response_code", length = 10)
    private String responseCode;    // vnp_ResponseCode

    @Column(name = "pay_url", length = 1000)
    private String payUrl;          // URL để FE redirect sang VNPay
}

