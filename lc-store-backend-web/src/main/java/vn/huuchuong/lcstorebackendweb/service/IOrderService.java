package vn.huuchuong.lcstorebackendweb.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.payload.request.order.CheckoutRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderResponse;

import java.util.List;

public interface IOrderService  {
    Page<Order> getAll(Pageable pageable);

    OrderResponse checkout(CheckoutRequest request);

    OrderResponse getOrderById(Integer orderId);

    List<OrderResponse> getMyOrders();
}
