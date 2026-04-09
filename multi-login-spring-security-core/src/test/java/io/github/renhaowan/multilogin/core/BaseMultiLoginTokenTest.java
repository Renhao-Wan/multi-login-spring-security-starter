package io.github.renhaowan.multilogin.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseMultiLoginToken 单元测试
 *
 * @author wan
 */
@DisplayName("BaseMultiLoginToken 测试")
class BaseMultiLoginTokenTest {

    private Map<String, Object> allParams;
    private List<String> principalParamName;
    private List<String> credentialParamName;
    private static final String CLIENT_TYPE = "customer";

    @BeforeEach
    void setUp() {
        allParams = new HashMap<>();
        allParams.put("username", "testuser");
        allParams.put("password", "testpass");
        allParams.put("phone", "13800138000");

        principalParamName = List.of("username", "phone");
        credentialParamName = List.of("password");
    }

    @Nested
    @DisplayName("未认证令牌创建测试")
    class UnauthenticatedTokenTests {

        @Test
        @DisplayName("创建未认证令牌")
        void shouldCreateUnauthenticatedToken() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertEquals(allParams, token.getAllParams());
            assertEquals(CLIENT_TYPE, token.getClientType());
            assertEquals(principalParamName, token.getPrincipalParamName());
            assertEquals(credentialParamName, token.getCredentialParamName());
            assertFalse(token.isAuthenticated());
            assertNull(token.getPrincipalDetails());
        }

        @Test
        @DisplayName("getCredentials 应返回所有参数")
        void shouldReturnAllParamsAsCredentials() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertEquals(allParams, token.getCredentials());
        }

        @Test
        @DisplayName("未认证时 getPrincipal 应返回所有参数")
        void shouldReturnAllParamsAsPrincipalWhenUnauthenticated() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertEquals(allParams, token.getPrincipal());
        }
    }

    @Nested
    @DisplayName("已认证令牌创建测试")
    class AuthenticatedTokenTests {

        @Test
        @DisplayName("创建已认证令牌")
        void shouldCreateAuthenticatedToken() {
            Object principalDetails = "user-details-object";
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );

            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName,
                    principalDetails, authorities
            );

            assertTrue(token.isAuthenticated());
            assertEquals(principalDetails, token.getPrincipalDetails());
            assertEquals(2, token.getAuthorities().size());
        }

        @Test
        @DisplayName("已认证时 getPrincipal 应返回 principalDetails")
        void shouldReturnPrincipalDetailsAsPrincipalWhenAuthenticated() {
            Object principalDetails = "user-details-object";

            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName,
                    principalDetails, Collections.emptyList()
            );

            assertEquals(principalDetails, token.getPrincipal());
        }

        @Test
        @DisplayName("空权限列表创建已认证令牌")
        void shouldCreateAuthenticatedTokenWithEmptyAuthorities() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName,
                    "principal", Collections.emptyList()
            );

            assertTrue(token.isAuthenticated());
            assertTrue(token.getAuthorities().isEmpty());
        }
    }

    @Nested
    @DisplayName("setPrincipalDetails 方法测试")
    class SetPrincipalDetailsTests {

        @Test
        @DisplayName("设置 principalDetails 后应标记为已认证")
        void shouldMarkAsAuthenticatedWhenPrincipalDetailsSet() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertFalse(token.isAuthenticated());

            token.setPrincipalDetails("user-details");

            assertTrue(token.isAuthenticated());
            assertEquals("user-details", token.getPrincipalDetails());
        }

        @Test
        @DisplayName("设置 principalDetails 后 getPrincipal 应返回新值")
        void shouldReturnNewPrincipalAfterSet() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            Object newPrincipal = new Object();
            token.setPrincipalDetails(newPrincipal);

            assertEquals(newPrincipal, token.getPrincipal());
        }

        @Test
        @DisplayName("可以多次设置 principalDetails")
        void canSetPrincipalDetailsMultipleTimes() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            token.setPrincipalDetails("first");
            token.setPrincipalDetails("second");

            assertEquals("second", token.getPrincipalDetails());
            assertTrue(token.isAuthenticated());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("参数为 null 时应正常处理")
        void shouldHandleNullParams() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    null, null, null, null
            );

            assertNull(token.getAllParams());
            assertNull(token.getClientType());
            assertNull(token.getPrincipalParamName());
            assertNull(token.getCredentialParamName());
        }

        @Test
        @DisplayName("空参数 Map 应正常处理")
        void shouldHandleEmptyParamsMap() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    Collections.emptyMap(), CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertTrue(token.getAllParams().isEmpty());
        }

        @Test
        @DisplayName("空 principalParamName 和 credentialParamName 应正常处理")
        void shouldHandleEmptyParamNames() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, Collections.emptyList(), Collections.emptyList()
            );

            assertTrue(token.getPrincipalParamName().isEmpty());
            assertTrue(token.getCredentialParamName().isEmpty());
        }

        @Test
        @DisplayName("getCredentials 返回的是原始 Map 引用")
        void shouldReturnOriginalParamsMap() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertSame(allParams, token.getCredentials());
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("应继承自 AbstractAuthenticationToken")
        void shouldBeInstanceOfAbstractAuthenticationToken() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertTrue(token instanceof org.springframework.security.authentication.AbstractAuthenticationToken);
        }

        @Test
        @DisplayName("未认证令牌的 authorities 应为空")
        void shouldHaveEmptyAuthoritiesForUnauthenticatedToken() {
            BaseMultiLoginToken token = new BaseMultiLoginToken(
                    allParams, CLIENT_TYPE, principalParamName, credentialParamName
            );

            assertTrue(token.getAuthorities().isEmpty());
        }
    }
}