package vn.huuchuong.lcstorebackendweb.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;

@RestControllerAdvice
public class GlobalHandlerException {

    // bat exception
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handlerBussinessException (BusinessException exception, ServletWebRequest servletWebRequest){
        return ResponseEntity.badRequest().body(new BaseResponse<>(null,exception.getMessage()));
    }



    // neu ko bat dc thi se toi day
    // bat exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handlerBussinessException (Exception exception, ServletWebRequest servletWebRequest){
        return ResponseEntity.badRequest().body(new BaseResponse<>(null,"Loi he thong vui long chay lai sau ( loi chua duoc xu ly "));
    }

}
