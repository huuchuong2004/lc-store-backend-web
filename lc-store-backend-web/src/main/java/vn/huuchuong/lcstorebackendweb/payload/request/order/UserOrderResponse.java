package vn.huuchuong.lcstorebackendweb.payload.request.order;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@Builder

public class UserOrderResponse {

    private Integer orderId;
    private UUID userId;
    private String username;
    private String fisrtName;
    private String lastName;
    private String email;
    private String phone;
}
