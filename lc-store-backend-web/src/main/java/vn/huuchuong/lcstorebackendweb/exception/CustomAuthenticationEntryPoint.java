package vn.huuchuong.lcstorebackendweb.exception;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import vn.huuchuong.lcstorebackendweb.base.BaseResponse;


import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint { // class nay de thng bao cho front end la ôtken het han hay goi api /refesh /

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        BaseResponse<?> body = BaseResponse.error("Access token không hợp lệ hoặc đã hết hạn");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
