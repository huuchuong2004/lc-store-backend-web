package vn.huuchuong.lcstorebackendweb.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private T data;
    private String message;



    // Khai báo 1 kiểu để đồng nhất kiểu dưx liệu trả về cho frontend


    // Helper cho code gọn hơn

    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(data, message);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data, "Success");
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(null, message);
    }


}
