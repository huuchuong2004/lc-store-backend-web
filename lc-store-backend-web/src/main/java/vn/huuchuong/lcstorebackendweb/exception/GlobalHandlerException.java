package vn.huuchuong.lcstorebackendweb.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandlerException {

    // 1. Lỗi validate từ @Valid @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {

        // Lấy message đầu tiên (hoặc bạn có thể gom tất cả)
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)  // chính là message trong @Email, @Size,...
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");

        BaseResponse<Object> response = new BaseResponse<>(null, message);
        return ResponseEntity.badRequest().body(response);
    }

    // 2. BusinessException
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handlerBusiness(BusinessException exception,
                                                                ServletWebRequest servletWebRequest) {
        return ResponseEntity
                .badRequest()
                .body(new BaseResponse<>(null, exception.getMessage()));
    }

    // 3. TransactionSystemException (DB, Hibernate...)
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<BaseResponse<Object>> handleTx(TransactionSystemException ex) {
        Throwable root = ex.getMostSpecificCause();
        root.printStackTrace();

        BaseResponse<Object> res = new BaseResponse<>();
        res.setData(null);
        res.setMessage(root.getMessage());

        return ResponseEntity.badRequest().body(res);
    }

    // 4. Cuối cùng mới bắt Exception chung chung
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleSystem(Exception exception,
                                                             ServletWebRequest servletWebRequest) {
        exception.printStackTrace();
        return ResponseEntity.badRequest()
                .body(new BaseResponse<>(null,
                        "Lỗi hệ thống, vui lòng chạy lại sau (lỗi chưa được xử lý riêng)"));
    }
}
