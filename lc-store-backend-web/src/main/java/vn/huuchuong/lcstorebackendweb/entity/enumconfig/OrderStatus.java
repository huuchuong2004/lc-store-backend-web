package vn.huuchuong.lcstorebackendweb.entity.enumconfig;

public enum OrderStatus {
    PENDING,          // vừa tạo đơn
    CONFIRMED,        // shop đã xác nhận
    SHIPPING,         // đang giao
    DELIVERED,        // giao thành công
    CANCELED          // hủy đơn
}