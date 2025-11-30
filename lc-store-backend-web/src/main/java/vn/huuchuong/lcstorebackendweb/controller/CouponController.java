package vn.huuchuong.lcstorebackendweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;
import vn.huuchuong.lcstorebackendweb.payload.request.coupon.CreateCouponRequest;
import vn.huuchuong.lcstorebackendweb.payload.response.CouponResponse;
import vn.huuchuong.lcstorebackendweb.service.ICouponService;
import vn.huuchuong.lcstorebackendweb.service.impl.CouponService;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final ICouponService couponService;

    @GetMapping
    public BaseResponse<Page<Coupon>>  getCoupons(Pageable pageable) {
        return BaseResponse.success(couponService.getCoupons(pageable), "Lấy danh sách coupon thành công");
    }

    @PostMapping
    public BaseResponse<CouponResponse> createCoupon(CreateCouponRequest request) {
        return BaseResponse.success(couponService.createCoupon(request), "Tạo coupon thành công");
    }

    @DeleteMapping
    public BaseResponse<Boolean> deleteCoupon(@RequestParam("id") Integer id) {

        return BaseResponse.success(couponService.deleteCoupon(id), "Xoá coupon thành công");
    }
}
