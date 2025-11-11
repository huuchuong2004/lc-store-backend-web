package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface IUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    // 🔹 Kiểm tra username đã tồn tại (cho register hoặc validate)
    boolean existsByUsername(String username);

    // 🔹 Kiểm tra email đã tồn tại (nếu có field email)
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);


}
