package vn.huuchuong.lcstorebackendweb.entity.enumconfig;

public enum OrderStatus {
    CREATED,          // đã tạo đơn nhưng chưa thanh toán
    PENDING,          // vừa tạo đơn
    CONFIRMED,        // shop đã xác nhận
    SHIPPING,         // đang giao
    DELIVERED,        // giao thành công
    CANCELED          // hủy đơn
}