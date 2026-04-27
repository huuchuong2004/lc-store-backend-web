package vn.huuchuong.lcstorebackendweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.payload.request.order.CheckoutRequest;
import vn.huuchuong.lcstorebackendweb.payload.request.order.OrderResponse;
import vn.huuchuong.lcstorebackendweb.payload.request.order.UserOrderResponse;
import vn.huuchuong.lcstorebackendweb.service.IOrderService;
import vn.huuchuong.lcstorebackendweb.service.impl.OrderService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final IOrderService orderService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Page<OrderResponse>>> getAllOrders(Pageable pageable) {
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
    public ResponseEntity<BaseResponse<Page<OrderResponse>>> getMyOrders(Pageable pageable) {

        Page<OrderResponse> res = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(BaseResponse.success(res, "Lấy danh sách đơn hàng của bạn thành công"));
    }
    @PostMapping("/{orderId}/cancel")

    public ResponseEntity<BaseResponse<OrderResponse>> cancelOrder(
            @PathVariable Integer orderId) {

        OrderResponse res = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(BaseResponse.success(res, "Huỷ đơn hàng thành công"));
    }

    @GetMapping("/admin/details/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<OrderResponse>> getOrdersDetailsWithAdminRole( @PathVariable Integer orderId) {
        OrderResponse res = orderService.getDetailsAdminRole(orderId);
        return ResponseEntity.ok(BaseResponse.success(res, "Lấy chi tiết đơn hàng thành công"));
    }


    @GetMapping("user/{orderId}")
    public ResponseEntity<BaseResponse<UserOrderResponse>> getUserByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(
                BaseResponse.success(orderService.getUserByOrderId(orderId), "Lấy Thong Tin Khach Hangthành công"));
    }

    @PutMapping("/{orderId}/update-status-shipping/")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> setStatusIsShipping(
            @PathVariable("orderId") Integer orderId
            ) {

        boolean result = ((OrderService) orderService).setStatusIsShipping(orderId);
        return ResponseEntity.ok(BaseResponse.success(result, "Cập nhật trạng thái đơn hàng thành công (Dand giao hang)"));
    }

    @PutMapping("/{orderId}/update-status-delivered/")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Boolean>> setStatusIsDelivered(@PathVariable Integer orderId) {
        boolean result = ((OrderService) orderService).setStatusIsDelivered(orderId);
        return ResponseEntity.ok(BaseResponse.success(result, "Cập nhật trạng thái đơn hàng thành công (Da giao hang thanh cong)"));
    }

    @GetMapping("/count-by-status-deliverred")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Integer>> countOrdersByStatusDeliverred() {
        int count =  orderService.countOrdersByStatusDeliverred();
        return ResponseEntity.ok(BaseResponse.success(count, "Đếm số lượng đơn hàng đã giao thành công thành công"));
    }

    @GetMapping("/count-orders")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<Integer>> countTotalOrders() {
        int count =  orderService.countTotalOrders();
        return ResponseEntity.ok(BaseResponse.success(count, "Đếm tổng số lượng đơn hàng thành công"));
    }

    @GetMapping("/revenue/total")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BaseResponse<BigDecimal>> getTotalRevenue() {
        BigDecimal revenue =  orderService.getTotalRevenue();
        return ResponseEntity.ok(BaseResponse.success(revenue, "Lấy tổng doanh thu thành công"));
    }

    @GetMapping("/getAddress")
    public ResponseEntity<BaseResponse<List<String>>> getAddressFromOrder() {
        return ResponseEntity.ok(BaseResponse.success(orderService.getAddressFromOrder(), "Lấy địa chỉ giao hàng thành công"));
    }




}
