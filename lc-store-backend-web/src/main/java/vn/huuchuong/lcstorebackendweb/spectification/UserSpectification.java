package vn.huuchuong.lcstorebackendweb.spectification;

import org.springframework.data.jpa.domain.Specification;
import vn.huuchuong.lcstorebackendweb.entity.User;

public class UserSpectification {

    public static Specification<User> hasUsername(String username) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%"+ username +"%");

        };

    }

    public static Specification<User> hasFirstName(String firstName) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%"+ firstName +"%");

        };

    }
    public static Specification<User> hasLastName( String lastName) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%"+ lastName +"%");

        };

    }
    public static Specification<User> hasAddress( String address) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%"+ address +"%");

        };

    }
    public static Specification<User> hasPhone( String phone) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), "%"+ phone +"%");

        };

    }
    public static Specification<User> hasEmail( String email) {

        // cu phap , root , query , builder
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%"+ email +"%");

        };

    }
}
