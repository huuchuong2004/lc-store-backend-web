package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.entity.User;

import java.util.List;
import java.util.Optional;

public interface IOrderRepository extends JpaRepository<Order, Integer> {
    @Query("""
           select distinct o from Order o
           left join fetch o.items i
           left join fetch i.productVariant v
           left join fetch v.product p
           where o.orderId = :id
           """)
    Optional<Order> findByIdFetchItems(Integer id);
 // giup load du lieu lien quan trong 1 query duy nhat
    @Query("""
           select distinct o from Order o
           left join fetch o.items i
           left join fetch i.productVariant v
           left join fetch v.product p
           where o.user = :user
           order by o.orderDate desc
           """)
    List<Order> findByUserFetchItems(User user);
}
