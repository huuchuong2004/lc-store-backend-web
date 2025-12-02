package vn.huuchuong.lcstorebackendweb.payload.response;

import jakarta.persistence.*;
import vn.huuchuong.lcstorebackendweb.entity.InvoiceItem;
import vn.huuchuong.lcstorebackendweb.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InvoiceResponse {


    private Integer invoiceId;



    private Order order;


    private LocalDate invoiceDate;


    private BigDecimal totalAmount;



    private String buyerName;


    private String buyerAddress;


    private String buyerEmail;


    private String buyerPhone;


    private List<InvoiceItem> items;
}
