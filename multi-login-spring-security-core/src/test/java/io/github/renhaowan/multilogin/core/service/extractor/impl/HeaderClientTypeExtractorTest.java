package io.github.renhaowan.multilogin.core.service.extractor.impl;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HeaderClientTypeExtractor 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HeaderClientTypeExtractor 测试")
class HeaderClientTypeExtractorTest {

    private HeaderClientTypeExtractor extractor;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @Mock
    private HttpServletRequest request;

    private GlobalConfig globalConfig;
    private LoginMethodConfig config;

    @BeforeEach
    void setUp() {
        extractor = new HeaderClientTypeExtractor(messageSourceHelper);
        globalConfig = new GlobalConfig();
        config = new LoginMethodConfig();
    }

    @Nested
    @DisplayName("正常提取测试")
    class NormalExtractionTests {

        @Test
        @DisplayName("应正确从请求头提取客户端类型")
        void shouldExtractClientTypeFromHeader() {
            globalConfig.setClientTypes(List.of("customer", "employee"));
            globalConfig.setRequestClientHeader("X-Client-Type");
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("X-Client-Type")).thenReturn("customer");

            String result = extractor.extractClientType(request);

            assertEquals("customer", result);
        }

        @Test
        @DisplayName("应使用方法级配置覆盖全局配置")
        void shouldUseMethodConfigOverGlobalConfig() {
            config.setRequestClientHeader("X-Custom-Header");
            config.setClientTypes(List.of("admin", "user"));
            extractor.setConfig(config);
            extractor.setGlobalConfig(globalConfig);

            when(request.getHeader("X-Custom-Header")).thenReturn("admin");

            String result = extractor.extractClientType(request);

            assertEquals("admin", result);
        }
    }

    @Nested
    @DisplayName("默认值逻辑测试")
    class DefaultValueTests {

        @Test
        @DisplayName("请求头不存在时应返回默认客户端类型")
        void shouldReturnDefaultClientTypeWhenHeaderMissing() {
            globalConfig.setClientTypes(List.of("customer", "employee"));
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("request-client")).thenReturn(null);
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning message");

            String result = extractor.extractClientType(request);

            assertEquals("customer", result);
        }

        @Test
        @DisplayName("客户端类型不在列表中时应返回默认类型")
        void shouldReturnDefaultClientTypeWhenTypeNotInList() {
            globalConfig.setClientTypes(List.of("customer", "employee"));
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("request-client")).thenReturn("unknown");
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning message");

            String result = extractor.extractClientType(request);

            assertEquals("customer", result);
        }

        @Test
        @DisplayName("方法级客户端类型列表应覆盖全局配置")
        void shouldUseMethodClientTypesForDefault() {
            config.setClientTypes(List.of("admin", "superadmin"));
            extractor.setConfig(config);
            extractor.setGlobalConfig(globalConfig);

            when(request.getHeader("request-client")).thenReturn(null);
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning message");

            String result = extractor.extractClientType(request);

            assertEquals("admin", result);
        }
    }

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @Test
        @DisplayName("客户端类型列表为空且请求头不存在时应抛出异常")
        void shouldThrowExceptionWhenNoClientTypesAndHeaderMissing() {
            globalConfig.setClientTypes(Collections.emptyList());
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("request-client")).thenReturn(null);

            assertThrows(MultiLoginException.class, () -> {
                extractor.extractClientType(request);
            });
        }

        @Test
        @DisplayName("客户端类型列表为 null 且请求头不存在时应抛出 NullPointerException")
        void shouldThrowNullPointerExceptionWhenClientTypesNullAndHeaderMissing() {
            globalConfig.setClientTypes(null);
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("request-client")).thenReturn(null);

            // 源代码对 null 处理不完善，会抛出 NullPointerException
            assertThrows(NullPointerException.class, () -> {
                extractor.extractClientType(request);
            });
        }
    }

    @Nested
    @DisplayName("配置优先级测试")
    class ConfigPriorityTests {

        @Test
        @DisplayName("方法级 requestClientHeader 应覆盖全局配置")
        void methodHeaderShouldOverrideGlobal() {
            globalConfig.setRequestClientHeader("X-Global-Header");
            config.setRequestClientHeader("X-Method-Header");
            config.setClientTypes(List.of("test"));
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("X-Method-Header")).thenReturn("test");

            String result = extractor.extractClientType(request);

            assertEquals("test", result);
            verify(request, never()).getHeader("X-Global-Header");
        }

        @Test
        @DisplayName("全局配置应作为默认值")
        void globalConfigShouldBeDefault() {
            globalConfig.setRequestClientHeader("X-Global-Header");
            globalConfig.setClientTypes(List.of("default-type"));
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("X-Global-Header")).thenReturn("default-type");

            String result = extractor.extractClientType(request);

            assertEquals("default-type", result);
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("请求头值为空字符串时应返回默认类型")
        void shouldReturnDefaultWhenHeaderEmpty() {
            globalConfig.setClientTypes(List.of("customer"));
            extractor.setGlobalConfig(globalConfig);
            extractor.setConfig(config);

            when(request.getHeader("request-client")).thenReturn("");
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            String result = extractor.extractClientType(request);

            assertEquals("customer", result);
        }

        @Test
        @DisplayName("配置未设置时应使用默认值")
        void shouldUseDefaultWhenConfigNotSet() {
            extractor.setConfig(config);
            extractor.setGlobalConfig(globalConfig);

            when(request.getHeader("request-client")).thenReturn("DEFAULT");

            String result = extractor.extractClientType(request);

            assertEquals("DEFAULT", result);
        }
    }
}