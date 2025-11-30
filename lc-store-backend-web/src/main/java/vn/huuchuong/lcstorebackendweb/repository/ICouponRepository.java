package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;

import java.util.Optional;

public interface ICouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCouponCode(String code);
}
