package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;
import vn.huuchuong.lcstorebackendweb.entity.CouponUsage;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.payload.request.coupon.CreateCouponRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.CouponResponse;
import vn.huuchuong.lcstorebackendweb.repository.ICouponRepository;
import vn.huuchuong.lcstorebackendweb.repository.IOrderRepository;
import vn.huuchuong.lcstorebackendweb.service.ICouponService;
import vn.huuchuong.lcstorebackendweb.service.IOrderService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService  implements ICouponService {

    private final ICouponRepository couponRepository;
    private final IOrderRepository orderRepository;
    @Override
    public Page<CouponResponse> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public CouponResponse createCoupon(CreateCouponRequest request) {

        if (couponRepository.findByCouponCode(request.getCouponCode()).isPresent()) {
            throw new RuntimeException("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        coupon.setCouponCode(request.getCouponCode());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setStartDate(LocalDate.now());
        coupon.setEndDate(request.getEndDate());
        coupon.setMinimumOrderAmount(request.getMinimumOrderAmount());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        coupon.setCurrentUsage(0);
        Coupon savedCoupon = couponRepository.save(coupon);
        return toResponse(savedCoupon);

    }

    @Override
    @Transactional
    public Boolean deleteCoupon(Integer id) {
        Coupon couponToDelete = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // vẫn nên ngắt Order vì Order.coupon là optional
        List<Order> ordersUsingCoupon = orderRepository.findByCoupon(couponToDelete);
        for (Order order : ordersUsingCoupon) {
            order.setCoupon(null);
        }
        orderRepository.saveAll(ordersUsingCoupon);

        couponRepository.delete(couponToDelete); // DB tự xóa coupon_usage

        return true;
    }




    private CouponResponse toResponse(Coupon c) {
        return CouponResponse.builder()
                .couponId(c.getCouponId())
                .couponCode(c.getCouponCode())
                .discountValue(c.getDiscountValue())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .minimumOrderAmount(c.getMinimumOrderAmount())
                .maxUsage(c.getMaxUsage())
                .maxUsagePerUser(c.getMaxUsagePerUser())
                .currentUsage(c.getCurrentUsage())
                .build();
    }
}
