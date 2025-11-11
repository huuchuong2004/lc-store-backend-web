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


}
