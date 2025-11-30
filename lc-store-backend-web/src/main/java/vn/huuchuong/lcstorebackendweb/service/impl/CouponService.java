package vn.huuchuong.lcstorebackendweb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;
import vn.huuchuong.lcstorebackendweb.payload.request.coupon.CreateCouponRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.CouponResponse;
import vn.huuchuong.lcstorebackendweb.repository.ICouponRepository;
import vn.huuchuong.lcstorebackendweb.service.ICouponService;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService  implements ICouponService {

    private final ICouponRepository couponRepository;
    @Override
    public Page<Coupon> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable);
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
    public Boolean deleteCoupon(Integer id) {

       if (!couponRepository.existsById(id)) {
           throw new RuntimeException("Coupon not found");
       }
         couponRepository.deleteById(id);
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
