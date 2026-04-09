package io.github.renhaowan.multilogin.core;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.service.BusinessAuthenticationLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RouterAuthenticationProvider 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RouterAuthenticationProvider 测试")
class RouterAuthenticationProviderTest {

    private RouterAuthenticationProvider provider;

    @Mock
    private BusinessAuthenticationLogic businessLogic1;

    @Mock
    private BusinessAuthenticationLogic businessLogic2;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    private static final String CLIENT_TYPE_CUSTOMER = "customer";
    private static final String CLIENT_TYPE_EMPLOYEE = "employee";

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("应正确映射 clientType 和 BusinessAuthenticationLogic")
        void shouldMapClientTypeToProvider() {
            provider = new RouterAuthenticationProvider(
                    List.of(businessLogic1, businessLogic2),
                    List.of(CLIENT_TYPE_CUSTOMER, CLIENT_TYPE_EMPLOYEE),
                    messageSourceHelper
            );

            // 通过 authenticate 方法验证映射是否正确
            BaseMultiLoginToken token = createToken(CLIENT_TYPE_CUSTOMER);
            when(businessLogic1.authenticate(any())).thenReturn("principal");

            assertDoesNotThrow(() -> provider.authenticate(token));
        }

        @Test
        @DisplayName("clientTypes 和 providers 数量应一致")
        void shouldAcceptMatchingSizeLists() {
            assertDoesNotThrow(() -> new RouterAuthenticationProvider(
                    List.of(businessLogic1, businessLogic2),
                    List.of(CLIENT_TYPE_CUSTOMER, CLIENT_TYPE_EMPLOYEE),
                    messageSourceHelper
            ));
        }
    }

    @Nested
    @DisplayName("authenticate 方法测试")
    class AuthenticateTests {

        @BeforeEach
        void setUpProvider() {
            provider = new RouterAuthenticationProvider(
                    List.of(businessLogic1, businessLogic2),
                    List.of(CLIENT_TYPE_CUSTOMER, CLIENT_TYPE_EMPLOYEE),
                    messageSourceHelper
            );
        }

        @Test
        @DisplayName("应正确路由到对应的 BusinessAuthenticationLogic")
        void shouldRouteToCorrectProvider() throws Exception {
            Map<String, Object> params = Map.of("username", "testuser");
            BaseMultiLoginToken token = createToken(CLIENT_TYPE_CUSTOMER, params);

            when(businessLogic1.authenticate(params)).thenReturn("userPrincipal");

            Authentication result = provider.authenticate(token);

            assertTrue(result.isAuthenticated());
            assertEquals("userPrincipal", ((BaseMultiLoginToken) result).getPrincipalDetails());
            verify(businessLogic1).authenticate(params);
            verify(businessLogic2, never()).authenticate(any());
        }

        @Test
        @DisplayName("不同的 clientType 应路由到不同的 Provider")
        void shouldRouteDifferentClientTypes() throws Exception {
            BaseMultiLoginToken customerToken = createToken(CLIENT_TYPE_CUSTOMER);
            BaseMultiLoginToken employeeToken = createToken(CLIENT_TYPE_EMPLOYEE);

            when(businessLogic1.authenticate(any())).thenReturn("customerPrincipal");
            when(businessLogic2.authenticate(any())).thenReturn("employeePrincipal");

            Authentication customerResult = provider.authenticate(customerToken);
            Authentication employeeResult = provider.authenticate(employeeToken);

            assertEquals("customerPrincipal", ((BaseMultiLoginToken) customerResult).getPrincipalDetails());
            assertEquals("employeePrincipal", ((BaseMultiLoginToken) employeeResult).getPrincipalDetails());
        }

        @Test
        @DisplayName("未知的 clientType 应抛出异常")
        void shouldThrowExceptionForUnknownClientType() {
            BaseMultiLoginToken token = createToken("unknown");

            assertThrows(MultiLoginException.class, () -> provider.authenticate(token));
        }

        @Test
        @DisplayName("BusinessAuthenticationLogic 返回 null 应抛出异常")
        void shouldThrowExceptionWhenPrincipalIsNull() {
            BaseMultiLoginToken token = createToken(CLIENT_TYPE_CUSTOMER);
            when(messageSourceHelper.getMessage(anyString())).thenReturn("error");
            when(businessLogic1.authenticate(any())).thenReturn(null);

            assertThrows(MultiLoginException.class, () -> provider.authenticate(token));
        }

        @Test
        @DisplayName("非 BaseMultiLoginToken 类型应返回 null")
        void shouldReturnNullForNonBaseMultiLoginToken() {
            Authentication otherToken = new UsernamePasswordAuthenticationToken("user", "pass");

            Authentication result = provider.authenticate(otherToken);

            assertNull(result);
        }

        @Test
        @DisplayName("认证成功后 token 应标记为已认证")
        void shouldMarkTokenAsAuthenticated() {
            BaseMultiLoginToken token = createToken(CLIENT_TYPE_CUSTOMER);

            when(businessLogic1.authenticate(any())).thenReturn("principal");

            Authentication result = provider.authenticate(token);

            assertTrue(result.isAuthenticated());
        }
    }

    @Nested
    @DisplayName("supports 方法测试")
    class SupportsTests {

        @BeforeEach
        void setUpProvider() {
            provider = new RouterAuthenticationProvider(
                    List.of(businessLogic1),
                    List.of(CLIENT_TYPE_CUSTOMER),
                    messageSourceHelper
            );
        }

        @Test
        @DisplayName("应支持 BaseMultiLoginToken 类型")
        void shouldSupportBaseMultiLoginToken() {
            assertTrue(provider.supports(BaseMultiLoginToken.class));
        }

        @Test
        @DisplayName("应支持 BaseMultiLoginToken 的子类")
        void shouldSupportSubclass() {
            class CustomToken extends BaseMultiLoginToken {
                public CustomToken(Map<String, Object> allParams, String clientType, 
                                   List<String> principalParamName, List<String> credentialParamName) {
                    super(allParams, clientType, principalParamName, credentialParamName);
                }
            }

            assertTrue(provider.supports(CustomToken.class));
        }

        @Test
        @DisplayName("不应支持其他 Authentication 类型")
        void shouldNotSupportOtherTypes() {
            assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
            assertFalse(provider.supports(Authentication.class));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @BeforeEach
        void setUpProvider() {
            provider = new RouterAuthenticationProvider(
                    List.of(businessLogic1),
                    List.of(CLIENT_TYPE_CUSTOMER),
                    messageSourceHelper
            );
        }

        @Test
        @DisplayName("BusinessAuthenticationLogic 抛出异常应传播")
        void shouldPropagateExceptionFromBusinessLogic() {
            BaseMultiLoginToken token = createToken(CLIENT_TYPE_CUSTOMER);

            when(businessLogic1.authenticate(any())).thenThrow(new RuntimeException("Business error"));

            assertThrows(RuntimeException.class, () -> provider.authenticate(token));
        }

        @Test
        @DisplayName("未知 clientType 应使用正确的错误码")
        void shouldUseCorrectErrorCodeForUnknownClientType() {
            BaseMultiLoginToken token = createToken("unknown");

            MultiLoginException exception = assertThrows(MultiLoginException.class, 
                    () -> provider.authenticate(token));

            assertNotNull(exception.getErrorCode());
        }
    }

    /**
     * 创建测试用的 Token
     */
    private BaseMultiLoginToken createToken(String clientType) {
        return createToken(clientType, Map.of("username", "test"));
    }

    private BaseMultiLoginToken createToken(String clientType, Map<String, Object> params) {
        return new BaseMultiLoginToken(
                params,
                clientType,
                List.of("username"),
                List.of("password")
        );
    }
}