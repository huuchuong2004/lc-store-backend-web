package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.User;

import java.util.UUID;

public interface IUserRepository extends JpaRepository<User, UUID> {

}
