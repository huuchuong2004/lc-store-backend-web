package vn.huuchuong.lcstorebackendweb.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;
import vn.huuchuong.lcstorebackendweb.payload.request.coupon.CreateCouponRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.CouponResponse;

public interface ICouponService {
    Page<Coupon> getCoupons(Pageable pageable);

    CouponResponse createCoupon(CreateCouponRequest request);


    Boolean deleteCoupon(Integer id);
}
