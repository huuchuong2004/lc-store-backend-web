package vn.huuchuong.lcstorebackendweb.payload.invoice;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceDTO {

    private Integer id;
    private LocalDateTime invoiceDate;
    private BigDecimal totalAmount;

    private OrderDTO order;
}
