package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;
import vn.huuchuong.lcstorebackendweb.entity.CouponUsage;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.entity.User;

public interface ICouponUsageRepository extends JpaRepository<CouponUsage, Integer> {
    long countByCoupon(Coupon coupon);

    long countByCouponAndUser(Coupon coupon, User user);

    boolean existsByOrder(Order order);
}
