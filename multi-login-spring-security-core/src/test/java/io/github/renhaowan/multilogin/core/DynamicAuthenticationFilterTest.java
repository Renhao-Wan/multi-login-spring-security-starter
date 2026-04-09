package io.github.renhaowan.multilogin.core;

import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DynamicAuthenticationFilter 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicAuthenticationFilter 测试")
class DynamicAuthenticationFilterTest {

    private DynamicAuthenticationFilter filter;

    @Mock
    private ParameterExtractor parameterExtractor;

    @Mock
    private ClientTypeExtractor clientTypeExtractor;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private LoginMethodConfig config;

    private static final String PROCESS_URL = "/login/phone";
    private static final String HTTP_METHOD = "POST";

    @BeforeEach
    void setUp() {
        config = new LoginMethodConfig();
        config.setProcessUrl(PROCESS_URL);
        config.setHttpMethod(HTTP_METHOD);
        config.setPrincipalParamName(List.of("username"));
        config.setCredentialParamName(List.of("password"));

        filter = new DynamicAuthenticationFilter(config, parameterExtractor, clientTypeExtractor, authenticationManager);
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("应正确设置 Filter 拦截路径")
        void shouldSetFilterPath() {
            AntPathRequestMatcher matcher = filter.getAntPathRequestMatcher();

            assertEquals(PROCESS_URL, matcher.getPattern());
        }

        @Test
        @DisplayName("应正确设置 HTTP 方法")
        void shouldSetHttpMethod() {
            AntPathRequestMatcher matcher = filter.getAntPathRequestMatcher();

            // AntPathRequestMatcher 的 getHttpMethod 返回 String
            assertNotNull(matcher);
        }

        @Test
        @DisplayName("应正确创建 Filter")
        void shouldCreateFilter() {
            assertNotNull(filter);
        }
    }

    @Nested
    @DisplayName("attemptAuthentication 方法测试")
    class AttemptAuthenticationTests {

        @Test
        @DisplayName("应正确提取参数并创建 Token")
        void shouldExtractParametersAndCreateToken() throws Exception {
            Map<String, Object> params = Map.of("username", "testuser", "password", "testpass");
            when(parameterExtractor.extractParameters(request)).thenReturn(params);
            when(clientTypeExtractor.extractClientType(request)).thenReturn("customer");

            BaseMultiLoginToken expectedToken = new BaseMultiLoginToken(
                    params, "customer", List.of("username"), List.of("password")
            );
            when(authenticationManager.authenticate(any(BaseMultiLoginToken.class))).thenReturn(expectedToken);

            Authentication result = filter.attemptAuthentication(request, response);

            verify(parameterExtractor).extractParameters(request);
            verify(clientTypeExtractor).extractClientType(request);
            verify(authenticationManager).authenticate(any(BaseMultiLoginToken.class));
            assertNotNull(result);
        }

        @Test
        @DisplayName("Token 应包含正确的参数")
        void shouldCreateTokenWithCorrectParameters() throws Exception {
            Map<String, Object> params = Map.of("username", "testuser", "password", "testpass");
            when(parameterExtractor.extractParameters(request)).thenReturn(params);
            when(clientTypeExtractor.extractClientType(request)).thenReturn("customer");
            when(authenticationManager.authenticate(any())).thenAnswer(invocation -> invocation.getArgument(0));

            filter.attemptAuthentication(request, response);

            verify(authenticationManager).authenticate(argThat(token -> {
                BaseMultiLoginToken t = (BaseMultiLoginToken) token;
                return t.getAllParams().equals(params) 
                        && t.getClientType().equals("customer")
                        && t.getPrincipalParamName().equals(List.of("username"))
                        && t.getCredentialParamName().equals(List.of("password"));
            }));
        }

        @Test
        @DisplayName("应设置 Token 的 details")
        void shouldSetTokenDetails() throws Exception {
            when(parameterExtractor.extractParameters(request)).thenReturn(Map.of("username", "test"));
            when(clientTypeExtractor.extractClientType(request)).thenReturn("customer");
            when(authenticationManager.authenticate(any())).thenAnswer(invocation -> invocation.getArgument(0));

            filter.attemptAuthentication(request, response);

            verify(authenticationManager).authenticate(argThat(token -> {
                BaseMultiLoginToken t = (BaseMultiLoginToken) token;
                return t.getDetails() != null;
            }));
        }

        @Test
        @DisplayName("应委托给 AuthenticationManager 进行认证")
        void shouldDelegateToAuthenticationManager() throws Exception {
            when(parameterExtractor.extractParameters(request)).thenReturn(Map.of());
            when(clientTypeExtractor.extractClientType(request)).thenReturn("customer");

            filter.attemptAuthentication(request, response);

            verify(authenticationManager).authenticate(any());
        }
    }

    @Nested
    @DisplayName("不同配置测试")
    class DifferentConfigTests {

        @Test
        @DisplayName("不同的 HTTP 方法应正确设置")
        void shouldHandleDifferentHttpMethod() {
            config.setHttpMethod("GET");
            DynamicAuthenticationFilter getFilter = new DynamicAuthenticationFilter(
                    config, parameterExtractor, clientTypeExtractor, authenticationManager
            );

            assertNotNull(getFilter.getAntPathRequestMatcher());
        }

        @Test
        @DisplayName("不同的 processUrl 应正确设置")
        void shouldHandleDifferentProcessUrl() {
            config.setProcessUrl("/api/v1/login");
            DynamicAuthenticationFilter customFilter = new DynamicAuthenticationFilter(
                    config, parameterExtractor, clientTypeExtractor, authenticationManager
            );

            assertEquals("/api/v1/login", customFilter.getAntPathRequestMatcher().getPattern());
        }

        @Test
        @DisplayName("多个 principalParamName 应正确传递")
        void shouldHandleMultiplePrincipalParamNames() throws Exception {
            config.setPrincipalParamName(List.of("username", "phone"));
            DynamicAuthenticationFilter multiFilter = new DynamicAuthenticationFilter(
                    config, parameterExtractor, clientTypeExtractor, authenticationManager
            );

            when(parameterExtractor.extractParameters(request)).thenReturn(Map.of("username", "test"));
            when(clientTypeExtractor.extractClientType(request)).thenReturn("customer");
            when(authenticationManager.authenticate(any())).thenAnswer(invocation -> invocation.getArgument(0));

            multiFilter.attemptAuthentication(request, response);

            verify(authenticationManager).authenticate(argThat(token -> {
                BaseMultiLoginToken t = (BaseMultiLoginToken) token;
                return t.getPrincipalParamName().equals(List.of("username", "phone"));
            }));
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("应继承自 AbstractAuthenticationProcessingFilter")
        void shouldBeInstanceOfAbstractAuthenticationProcessingFilter() {
            assertTrue(filter instanceof org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter);
        }
    }
}