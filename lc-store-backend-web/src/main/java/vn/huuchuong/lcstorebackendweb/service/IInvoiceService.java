package vn.huuchuong.lcstorebackendweb.service;

import vn.huuchuong.lcstorebackendweb.entity.Invoice;
import vn.huuchuong.lcstorebackendweb.entity.Order;

public interface IInvoiceService {
    public Invoice createInvoiceForOrder(Order order);
    Invoice createInvoiceForOrderIfNotExists(Order order);
}
