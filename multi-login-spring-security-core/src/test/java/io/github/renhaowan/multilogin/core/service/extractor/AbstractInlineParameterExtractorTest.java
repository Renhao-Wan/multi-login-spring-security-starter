package io.github.renhaowan.multilogin.core.service.extractor;

import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AbstractInlineParameterExtractor 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractInlineParameterExtractor 测试")
class AbstractInlineParameterExtractorTest {

    private TestParameterExtractor extractor;
    private LoginMethodConfig config;

    @Mock
    private HttpServletRequest request;

    /**
     * 测试用的具体实现类
     */
    static class TestParameterExtractor extends AbstractInlineParameterExtractor {
        private Map<String, Object> lastExtractedParams;
        private Set<String> lastParamNames;

        @Override
        protected Map<String, Object> doExtractParameters(HttpServletRequest request, Set<String> paramNames) {
            this.lastParamNames = paramNames;
            this.lastExtractedParams = new HashMap<>();
            for (String paramName : paramNames) {
                lastExtractedParams.put(paramName, "extracted-" + paramName);
            }
            return lastExtractedParams;
        }
    }

    @BeforeEach
    void setUp() {
        extractor = new TestParameterExtractor();
        config = new LoginMethodConfig();
    }

    @Nested
    @DisplayName("extractParameters 模板方法测试")
    class TemplateMethodTests {

        @Test
        @DisplayName("应调用 doExtractParameters 并传递合并后的参数名")
        void shouldCallDoExtractParametersWithMergedParamNames() {
            config.setParamName(List.of("username", "password"));
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            extractor.setConfig(config);

            var result = extractor.extractParameters(request);

            assertNotNull(result);
            assertNotNull(extractor.lastParamNames);
            assertTrue(extractor.lastParamNames.contains("username"));
            assertTrue(extractor.lastParamNames.contains("password"));
        }

        @Test
        @DisplayName("应返回 doExtractParameters 的结果")
        void shouldReturnResultFromDoExtractParameters() {
            config.setParamName(List.of("test"));
            extractor.setConfig(config);

            var result = extractor.extractParameters(request);

            assertEquals("extracted-test", result.get("test"));
        }
    }

    @Nested
    @DisplayName("参数名合并去重测试")
    class ParamNameMergeTests {

        @Test
        @DisplayName("应合并 paramName、principalParamName 和 credentialParamName")
        void shouldMergeAllParamNames() {
            config.setParamName(List.of("extra1", "extra2"));
            config.setPrincipalParamName(List.of("username", "email"));
            config.setCredentialParamName(List.of("password", "captcha"));
            extractor.setConfig(config);

            extractor.extractParameters(request);

            Set<String> paramNames = extractor.lastParamNames;
            assertEquals(6, paramNames.size());
            assertTrue(paramNames.contains("extra1"));
            assertTrue(paramNames.contains("extra2"));
            assertTrue(paramNames.contains("username"));
            assertTrue(paramNames.contains("email"));
            assertTrue(paramNames.contains("password"));
            assertTrue(paramNames.contains("captcha"));
        }

        @Test
        @DisplayName("重复的参数名应去重")
        void shouldDeduplicateParamNames() {
            config.setParamName(List.of("username", "password"));
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            extractor.setConfig(config);

            extractor.extractParameters(request);

            assertEquals(2, extractor.lastParamNames.size());
        }

        @Test
        @DisplayName("三个列表中有相同参数名时应只保留一个")
        void shouldDeduplicateWhenSameParamInAllLists() {
            config.setParamName(List.of("token"));
            config.setPrincipalParamName(List.of("token"));
            config.setCredentialParamName(List.of("token"));
            extractor.setConfig(config);

            extractor.extractParameters(request);

            assertEquals(1, extractor.lastParamNames.size());
            assertTrue(extractor.lastParamNames.contains("token"));
        }
    }

    @Nested
    @DisplayName("空配置处理测试")
    class EmptyConfigTests {

        @Test
        @DisplayName("所有参数名列表为空时应返回空 Map")
        void shouldReturnEmptyMapWhenAllParamNamesEmpty() {
            config.setParamName(Collections.emptyList());
            config.setPrincipalParamName(Collections.emptyList());
            config.setCredentialParamName(Collections.emptyList());
            extractor.setConfig(config);

            var result = extractor.extractParameters(request);

            assertTrue(result.isEmpty());
            // 不应调用 doExtractParameters
            assertNull(extractor.lastParamNames);
        }

        @Test
        @DisplayName("配置为 null 时应抛出 NullPointerException")
        void shouldThrowExceptionWhenConfigNull() {
            extractor.setConfig(null);

            assertThrows(NullPointerException.class, () -> {
                extractor.extractParameters(request);
            });
        }

        @Test
        @DisplayName("paramName 为 null 但其他列表有值时应抛出 NullPointerException")
        void shouldThrowExceptionWhenParamNameNullWithOtherLists() {
            config.setParamName(null);
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            extractor.setConfig(config);

            assertThrows(NullPointerException.class, () -> {
                extractor.extractParameters(request);
            });
        }
    }

    @Nested
    @DisplayName("配置设置测试")
    class ConfigSetterTests {

        @Test
        @DisplayName("setConfig 应正确设置配置")
        void shouldSetConfigCorrectly() {
            LoginMethodConfig newConfig = new LoginMethodConfig();
            newConfig.setParamName(List.of("test"));

            extractor.setConfig(newConfig);

            assertSame(newConfig, extractor.config);
        }
    }
}