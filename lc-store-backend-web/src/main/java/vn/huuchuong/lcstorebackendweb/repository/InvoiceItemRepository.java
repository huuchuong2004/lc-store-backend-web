package vn.huuchuong.lcstorebackendweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.huuchuong.lcstorebackendweb.entity.Invoice;
import vn.huuchuong.lcstorebackendweb.entity.InvoiceItem;
import vn.huuchuong.lcstorebackendweb.entity.User;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Integer> {
    @Query("""
       select distinct i from Invoice i
       join fetch i.order o
       left join fetch o.items it
       left join fetch it.productVariant v
       left join fetch v.product p
       where o.user = :user
       order by o.orderDate desc
       """)
    List<Invoice> findByUserFetchOrderAndItems(User user);

    @Query("""
       select distinct i from Invoice i
       join fetch i.order o
       left join fetch o.items it
       left join fetch it.productVariant v
       left join fetch v.product p
       order by o.orderDate desc
       """)
    List<Invoice> findAllFetchOrderAndItems();

}
