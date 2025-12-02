package vn.huuchuong.lcstorebackendweb.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import vn.huuchuong.lcstorebackendweb.entity.Order;
import vn.huuchuong.lcstorebackendweb.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface IPaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByTxnRef(String txnRef);

    List<Payment> findByOrder(Order order);
}

