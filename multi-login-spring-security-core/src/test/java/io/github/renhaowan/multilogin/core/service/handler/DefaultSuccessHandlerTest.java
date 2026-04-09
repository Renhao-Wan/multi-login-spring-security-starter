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
import org.springframework.security.core.Authentication;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DefaultSuccessHandler 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DefaultSuccessHandler 测试")
class DefaultSuccessHandlerTest {

    private DefaultSuccessHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        handler = new DefaultSuccessHandler();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Nested
    @DisplayName("onAuthenticationSuccess 方法测试")
    class OnAuthenticationSuccessTests {

        @Test
        @DisplayName("应写入成功消息到响应")
        void shouldWriteSuccessMessage() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(response).getWriter();
            assertTrue(stringWriter.toString().contains("Success"));
        }

        @Test
        @DisplayName("应正确处理请求")
        void shouldHandleRequest() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);

            assertDoesNotThrow(() -> 
                handler.onAuthenticationSuccess(request, response, authentication)
            );
        }

        @Test
        @DisplayName("Authentication 参数应被接收")
        void shouldAcceptAuthenticationParameter() throws Exception {
            when(response.getWriter()).thenReturn(printWriter);

            // 即使 authentication 为 mock 对象，也不应影响处理
            assertDoesNotThrow(() -> 
                handler.onAuthenticationSuccess(request, response, authentication)
            );
        }
    }

    @Nested
    @DisplayName("接口实现测试")
    class InterfaceTests {

        @Test
        @DisplayName("应实现 AuthenticationSuccessHandler 接口")
        void shouldImplementAuthenticationSuccessHandler() {
            assertTrue(handler instanceof org.springframework.security.web.authentication.AuthenticationSuccessHandler);
        }
    }
}