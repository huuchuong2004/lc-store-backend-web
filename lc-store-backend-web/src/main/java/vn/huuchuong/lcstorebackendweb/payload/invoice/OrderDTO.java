package vn.huuchuong.lcstorebackendweb.payload.invoice;


import lombok.*;
import vn.huuchuong.lcstorebackendweb.entity.OrderItem;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Integer orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;


    private List<OrderItemDTO> items;

    private UserDTO user;
}
