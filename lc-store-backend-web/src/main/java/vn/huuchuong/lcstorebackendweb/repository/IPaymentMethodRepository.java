package vn.huuchuong.lcstorebackendweb.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.PaymentMethod;
import vn.huuchuong.lcstorebackendweb.entity.enumconfig.PaymentMethodType;


import java.util.Optional;

public interface IPaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    Optional<PaymentMethod> findByCode(PaymentMethodType code);
}

