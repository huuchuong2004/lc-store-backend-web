package vn.huuchuong.lcstorebackendweb.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.payload.request.order.CheckoutRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.order.UserOrderResponse;

import java.util.List;

public interface IOrderService  {
    Page<OrderResponse> getAll(Pageable pageable);

    OrderResponse checkout(CheckoutRequest request);

    OrderResponse getOrderById(Integer orderId);

    Page<OrderResponse> getMyOrders(Pageable pageable);

    OrderResponse cancelOrder(Integer orderId);

    OrderResponse getDetailsAdminRole(Integer orderId);

    UserOrderResponse getUserByOrderId(Integer orderId);
}
