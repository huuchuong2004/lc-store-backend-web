package vn.huuchuong.lcstorebackendweb.payload.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SetAdminRequest {

    @Size(min = 6, max = 30,message = "Do dai username chua phu hop 6- 30 !")
    @NotBlank(message = "Username not null !")
    private String username;
}
