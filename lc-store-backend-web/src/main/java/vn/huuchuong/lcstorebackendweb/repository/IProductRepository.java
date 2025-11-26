package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.huuchuong.lcstorebackendweb.entity.Product;

import java.util.List;

public interface IProductRepository extends JpaRepository<Product, Integer> , JpaSpecificationExecutor<Product> {
    Page<Product> findByCategory_IdIn(List<Integer> ids, Pageable pageable);

}
