package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.Cart;
import vn.huuchuong.lcstorebackendweb.entity.CartItem;
import vn.huuchuong.lcstorebackendweb.entity.ProductVariant;

import java.util.Optional;

public interface ICartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant variant);

    void deleteAllByCart(Cart cart);
}
