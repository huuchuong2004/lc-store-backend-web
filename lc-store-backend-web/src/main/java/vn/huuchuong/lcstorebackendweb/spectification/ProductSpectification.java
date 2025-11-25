package vn.huuchuong.lcstorebackendweb.spectification;

import org.springframework.data.jpa.domain.Specification;
import vn.huuchuong.lcstorebackendweb.entity.Product;
import vn.huuchuong.lcstorebackendweb.entity.User;

import java.math.BigDecimal;

public class ProductSpectification {
    public static Specification<Product> hasName(String name) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%"+ name +"%");

        };

    }

    public static Specification<Product> hasCategory(Integer id) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("id")), "%"+ id +"%");

        };

    }

    public static Specification<Product> hasMinPrice(BigDecimal minPrice) {
        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.greaterThanOrEqualTo(criteriaBuilder.lower(root.get("baseprice")), "%"+ minPrice +"%");

        };
    }

    public static Specification<Product> hasMaxPrice(BigDecimal maxPrice) {
        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.lessThanOrEqualTo(criteriaBuilder.lower(root.get("baseprice")), "%"+ maxPrice +"%");

        };
    }
}
