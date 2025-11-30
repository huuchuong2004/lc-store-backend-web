package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.huuchuong.lcstorebackendweb.entity.Cart;
import vn.huuchuong.lcstorebackendweb.entity.User;

import java.util.Optional;

public interface ICartRepository extends JpaRepository<Cart, Integer> {



        Optional<Cart> findByUser(User user);
        // 🔹 Lấy cart + items + variant + product bằng 1 query
        @Query("""
           select distinct c 
           from Cart c
           left join fetch c.items ci
           left join fetch ci.productVariant v
           left join fetch v.product p
           where c.user = :user
           """)
        Optional<Cart> findByUserFetchItems(User user);


}
