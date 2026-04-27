package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.huuchuong.lcstorebackendweb.entity.Coupon;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.entity.User;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IOrderRepository extends JpaRepository<Order, Integer>  {
    @Query("""
           select distinct o from Order o
           left join fetch o.items i
           left join fetch i.productVariant v
           left join fetch v.product p
           where o.orderId = :id
           """)
    Optional<Order> findByIdFetchItems(Integer id);
 // giup load du lieu lien quan trong 1 query duy nhat
 @Query(
         value = """
            select distinct o
            from Order o
            left join fetch o.items i
            left join fetch i.productVariant v
            left join fetch v.product p
            where o.user = :user
            order by o.orderDate desc
            """,
         countQuery = """
            select count(distinct o)
            from Order o
            where o.user = :user
            """
 )
 Page<Order> findByUserFetchItems(@Param("user") User user, Pageable pageable);

    List<Order> findByCoupon(Coupon couponToDelete);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :orderStatus")
    int countByStatus(OrderStatus orderStatus);

    @Query("SELECT COUNT(o) FROM Order o")
    int countAllOrders();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :orderStatus")
    BigDecimal sumTotalAmountByStatus(OrderStatus orderStatus);

    List<Order> findByUserId(UUID userId);

    @Query("""
    select distinct o from Order o
    left join fetch o.items i
    left join fetch i.productVariant pv
    left join fetch pv.product p
    left join fetch o.user u
    where o.orderId = :orderId
""")
    Optional<Order> findByIdWithItems(@Param("orderId") Integer orderId);
}
