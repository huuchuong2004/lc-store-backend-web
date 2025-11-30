package vn.huuchuong.lcstorebackendweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.payload.request.order.CheckoutRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderResponse;
import vn.huuchuong.lcstorebackendweb.service.IOrderService;
import vn.huuchuong.lcstorebackendweb.service.impl.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<Order>>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(
                BaseResponse.success(orderService.getAll(pageable), "Lấy danh sách đơn hàng thành công"));
    }


    @PostMapping("/checkout")
    public ResponseEntity<BaseResponse<OrderResponse>> checkout(@RequestBody CheckoutRequest request) {

        OrderResponse res = orderService.checkout(request);
        return ResponseEntity.ok(BaseResponse.success(res, "Tạo đơn hàng thành công"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<BaseResponse<OrderResponse>> getOrder(@PathVariable Integer orderId) {

        OrderResponse res = orderService.getOrderById(orderId);
        return ResponseEntity.ok(BaseResponse.success(res, "Lấy đơn hàng thành công"));
    }

    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getMyOrders() {

        List<OrderResponse> res = orderService.getMyOrders();
        return ResponseEntity.ok(BaseResponse.success(res, "Lấy danh sách đơn hàng của bạn thành công"));
    }


}
