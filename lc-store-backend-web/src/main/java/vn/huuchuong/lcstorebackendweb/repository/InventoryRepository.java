package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.huuchuong.lcstorebackendweb.entity.Inventory;
import vn.huuchuong.lcstorebackendweb.entity.ProductVariant;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    Optional<Inventory> findByProductVariant(ProductVariant variant);

}

