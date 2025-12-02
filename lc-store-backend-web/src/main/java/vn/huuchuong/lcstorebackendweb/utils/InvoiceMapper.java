package vn.huuchuong.lcstorebackendweb.utils;



import vn.huuchuong.lcstorebackendweb.payload.invoice.InvoiceDTO;
import vn.huuchuong.lcstorebackendweb.payload.invoice.OrderDTO;
import vn.huuchuong.lcstorebackendweb.payload.invoice.OrderItemDTO;
import vn.huuchuong.lcstorebackendweb.payload.invoice.UserDTO;
import vn.huuchuong.lcstorebackendweb.payload.request.*;
import vn.huuchuong.lcstorebackendweb.entity.*;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceMapper {

    public static InvoiceDTO toDto(Invoice invoice) {
        if (invoice == null) return null;

        return InvoiceDTO.builder()
                .id(invoice.getInvoiceId())
                .invoiceDate(invoice.getInvoiceDate())
                .totalAmount(invoice.getTotalAmount())
                .order(toDto(invoice.getOrder()))
                .build();
    }

    public static OrderDTO toDto(Order order) {
        if (order == null) return null;

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .user(toDto(order.getUser()))
                .items(
                        order.getItems() == null
                                ? List.of()
                                : order.getItems().stream()
                                .map(InvoiceMapper::toDto)
                                .toList()
                )
                .build();
    }

    public static OrderItemDTO toDto(OrderItem item) {
        if (item == null) return null;

        ProductVariant v = item.getProductVariant();

        BigDecimal discount = item.getDiscountAmount() != null
                ? item.getDiscountAmount()
                : BigDecimal.ZERO;

        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .subtract(discount);

        return OrderItemDTO.builder()
                .orderItemId(item.getOrderItemId())
                .productVariantId(v.getProductVariantId())
                .productName(v.getProduct().getName())
                .size(v.getSize())
                .color(v.getColor())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountAmount(item.getDiscountAmount())

                .build();
    }

    public static UserDTO toDto(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}

