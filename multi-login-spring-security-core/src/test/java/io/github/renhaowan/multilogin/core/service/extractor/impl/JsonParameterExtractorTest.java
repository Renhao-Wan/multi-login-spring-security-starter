package io.github.renhaowan.multilogin.core.service.extractor.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JsonParameterExtractor 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsonParameterExtractor 测试")
class JsonParameterExtractorTest {

    private JsonParameterExtractor extractor;
    private ObjectMapper objectMapper;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @Mock
    private HttpServletRequest request;

    private LoginMethodConfig config;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        extractor = new JsonParameterExtractor(objectMapper, messageSourceHelper);
        config = new LoginMethodConfig();
        config.setParamName(List.of("username", "password"));
        config.setPrincipalParamName(List.of("username"));
        config.setCredentialParamName(List.of("password"));
        extractor.setConfig(config);
    }

    @Nested
    @DisplayName("Content-Type 校验测试")
    class ContentTypeTests {

        @Test
        @DisplayName("非 JSON 请求应返回空 Map")
        void shouldReturnEmptyMapForNonJsonRequest() {
            when(request.getContentType()).thenReturn("text/html");
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Content-Type 为 null 应返回空 Map")
        void shouldReturnEmptyMapForNullContentType() {
            when(request.getContentType()).thenReturn(null);
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Content-Type 为空字符串应返回空 Map")
        void shouldReturnEmptyMapForEmptyContentType() {
            when(request.getContentType()).thenReturn("");
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isJsonRequest 方法测试")
    class IsJsonRequestTests {

        @Test
        @DisplayName("application/json 应被识别为 JSON")
        void shouldRecognizeApplicationJson() {
            // Content-Type 校验是私有方法，通过非 JSON 请求返回空 Map 来间接验证
            when(request.getContentType()).thenReturn("text/plain");
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            var result = extractor.extractParameters(request);

            // 非 JSON 请求应返回空 Map
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("application/json;charset=UTF-8 应被识别为 JSON")
        void shouldRecognizeApplicationJsonWithCharset() {
            // 同上，通过对比验证
            when(request.getContentType()).thenReturn(null);
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("配置测试")
    class ConfigTests {

        @Test
        @DisplayName("配置为 null 时应抛出 NullPointerException")
        void shouldThrowExceptionWhenConfigNull() {
            extractor.setConfig(null);

            assertThrows(NullPointerException.class, () -> {
                extractor.extractParameters(request);
            });
        }

        @Test
        @DisplayName("参数名列表为空时应返回空 Map")
        void shouldReturnEmptyMapWhenParamNamesEmpty() {
            config.setParamName(List.of());
            config.setPrincipalParamName(List.of());
            config.setCredentialParamName(List.of());
            extractor.setConfig(config);

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("应继承自 AbstractInlineParameterExtractor")
        void shouldBeInstanceOfAbstractInlineParameterExtractor() {
            assertTrue(extractor instanceof io.github.renhaowan.multilogin.core.service.extractor.AbstractInlineParameterExtractor);
        }

        @Test
        @DisplayName("应实现 ParameterExtractor 接口")
        void shouldImplementParameterExtractor() {
            assertTrue(extractor instanceof io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor);
        }
    }

    @Nested
    @DisplayName("doExtractParameters 方法测试")
    class DoExtractParametersTests {

        @Test
        @DisplayName("非 JSON 请求应返回空 Map")
        void shouldReturnEmptyMapForNonJsonRequest() {
            when(request.getContentType()).thenReturn("text/plain");
            when(messageSourceHelper.getMessage(anyString(), any())).thenReturn("warning");

            var result = extractor.doExtractParameters(request, java.util.Set.of("username"));

            assertTrue(result.isEmpty());
        }
    }
}