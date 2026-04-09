package io.github.renhaowan.multilogin.core.service.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultFailureHandler 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultFailureHandler 测试")
class DefaultFailureHandlerTest {

    private DefaultFailureHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        handler = new DefaultFailureHandler();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Nested
    @DisplayName("onAuthenticationFailure 方法测试")
    class OnAuthenticationFailureTests {

        @Test
        @DisplayName("应写入失败消息到响应")
        void shouldWriteFailureMessage() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);

            handler.onAuthenticationFailure(request, response, exception);

            verify(response).getWriter();
            assertTrue(stringWriter.toString().contains("Failure"));
        }

        @Test
        @DisplayName("应正确处理请求")
        void shouldHandleRequest() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);

            assertDoesNotThrow(() -> 
                handler.onAuthenticationFailure(request, response, exception)
            );
        }

        @Test
        @DisplayName("AuthenticationException 参数应被接收")
        void shouldAcceptAuthenticationExceptionParameter() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);

            // 即使 exception 为 mock 对象，也不应影响处理
            assertDoesNotThrow(() -> 
                handler.onAuthenticationFailure(request, response, exception)
            );
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("应实现 AuthenticationFailureHandler 接口")
        void shouldImplementAuthenticationFailureHandler() {
            assertTrue(handler instanceof org.springframework.security.web.authentication.AuthenticationFailureHandler);
        }
    }
}