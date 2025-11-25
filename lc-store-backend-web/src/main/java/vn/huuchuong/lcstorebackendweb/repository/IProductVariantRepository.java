package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.ProductVariant;

public interface IProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    boolean existsBySku(String sku);
}
