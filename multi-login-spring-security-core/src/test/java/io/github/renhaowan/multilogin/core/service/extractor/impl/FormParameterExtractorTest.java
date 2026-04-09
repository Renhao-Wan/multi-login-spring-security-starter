package io.github.renhaowan.multilogin.core.service.extractor.impl;

import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FormParameterExtractor 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FormParameterExtractor 测试")
class FormParameterExtractorTest {

    private FormParameterExtractor extractor;

    @Mock
    private HttpServletRequest request;

    private LoginMethodConfig config;

    @BeforeEach
    void setUp() {
        extractor = new FormParameterExtractor();
        config = new LoginMethodConfig();
        config.setParamName(java.util.List.of("username", "password", "phone"));
        config.setPrincipalParamName(java.util.List.of("username", "phone"));
        config.setCredentialParamName(java.util.List.of("password"));
        extractor.setConfig(config);
    }

    @Nested
    @DisplayName("参数提取测试")
    class ExtractParametersTests {

        @Test
        @DisplayName("应正确提取所有配置的参数")
        void shouldExtractAllConfiguredParameters() {
            when(request.getParameter("username")).thenReturn("testuser");
            when(request.getParameter("password")).thenReturn("testpass");
            when(request.getParameter("phone")).thenReturn("13800138000");

            var result = extractor.extractParameters(request);

            assertEquals(3, result.size());
            assertEquals("testuser", result.get("username"));
            assertEquals("testpass", result.get("password"));
            assertEquals("13800138000", result.get("phone"));
        }

        @Test
        @DisplayName("应忽略 null 值的参数")
        void shouldIgnoreNullParameters() {
            when(request.getParameter("username")).thenReturn("testuser");
            when(request.getParameter("password")).thenReturn(null);
            when(request.getParameter("phone")).thenReturn("13800138000");

            var result = extractor.extractParameters(request);

            assertEquals(2, result.size());
            assertTrue(result.containsKey("username"));
            assertTrue(result.containsKey("phone"));
            assertFalse(result.containsKey("password"));
        }

        @Test
        @DisplayName("所有参数都为 null 时应返回空 Map")
        void shouldReturnEmptyMapWhenAllParametersNull() {
            when(request.getParameter(anyString())).thenReturn(null);

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("应正确处理空字符串参数")
        void shouldHandleEmptyStringParameters() {
            config.setParamName(java.util.List.of("username", "password"));
            config.setPrincipalParamName(java.util.List.of("username"));
            config.setCredentialParamName(java.util.List.of("password"));
            extractor.setConfig(config);

            when(request.getParameter("username")).thenReturn("");
            when(request.getParameter("password")).thenReturn("testpass");

            var result = extractor.extractParameters(request);

            assertEquals(2, result.size());
            assertEquals("", result.get("username"));
            assertEquals("testpass", result.get("password"));
        }
    }

    @Nested
    @DisplayName("配置合并测试")
    class ConfigMergeTests {

        @Test
        @DisplayName("应合并 principalParamName 和 credentialParamName")
        void shouldMergePrincipalAndCredentialParams() {
            config.setParamName(java.util.List.of());
            config.setPrincipalParamName(java.util.List.of("username", "email"));
            config.setCredentialParamName(java.util.List.of("password", "captcha"));
            extractor.setConfig(config);

            when(request.getParameter("username")).thenReturn("user");
            when(request.getParameter("email")).thenReturn("test@test.com");
            when(request.getParameter("password")).thenReturn("pass");
            when(request.getParameter("captcha")).thenReturn("1234");

            var result = extractor.extractParameters(request);

            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("重复参数名应去重")
        void shouldDeduplicateParamNames() {
            config.setParamName(java.util.List.of("username", "password"));
            config.setPrincipalParamName(java.util.List.of("username"));
            config.setCredentialParamName(java.util.List.of("password"));
            extractor.setConfig(config);

            when(request.getParameter("username")).thenReturn("user");
            when(request.getParameter("password")).thenReturn("pass");

            var result = extractor.extractParameters(request);

            assertEquals(2, result.size());
            verify(request, times(1)).getParameter("username");
            verify(request, times(1)).getParameter("password");
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("配置为 null 时应抛出 NullPointerException")
        void shouldThrowExceptionWhenConfigIsNull() {
            extractor.setConfig(null);

            assertThrows(NullPointerException.class, () -> {
                extractor.extractParameters(request);
            });
        }

        @Test
        @DisplayName("参数名列表为空时应返回空 Map")
        void shouldReturnEmptyMapWhenParamNamesEmpty() {
            config.setParamName(java.util.List.of());
            config.setPrincipalParamName(java.util.List.of());
            config.setCredentialParamName(java.util.List.of());
            extractor.setConfig(config);

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("应处理特殊字符参数值")
        void shouldHandleSpecialCharacters() {
            config.setParamName(java.util.List.of("username", "password"));
            config.setPrincipalParamName(java.util.List.of("username"));
            config.setCredentialParamName(java.util.List.of("password"));
            extractor.setConfig(config);

            when(request.getParameter("username")).thenReturn("user@#$%");
            when(request.getParameter("password")).thenReturn("pass word");

            var result = extractor.extractParameters(request);

            assertEquals("user@#$%", result.get("username"));
            assertEquals("pass word", result.get("password"));
        }
    }

    @Nested
    @DisplayName("doExtractParameters 方法测试")
    class DoExtractParametersTests {

        @Test
        @DisplayName("直接调用 doExtractParameters 应正确工作")
        void shouldWorkWithDirectCall() {
            when(request.getParameter("username")).thenReturn("directuser");

            var result = extractor.doExtractParameters(request, Set.of("username"));

            assertEquals(1, result.size());
            assertEquals("directuser", result.get("username"));
        }
    }
}