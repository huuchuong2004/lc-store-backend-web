package vn.huuchuong.lcstorebackendweb.payload.response;
//import lombok.Builder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponUsageResponse {
    private Integer id;
    private Integer orderId; // avoid embedding whole Order entity
    private Integer couponId;

}
