package io.github.renhaowan.multilogin.core.properties;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MultiLoginProperties 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultiLoginProperties 测试")
class MultiLoginPropertiesTest {

    private MultiLoginProperties properties;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @BeforeEach
    void setUp() {
        properties = new MultiLoginProperties();
    }

    @Nested
    @DisplayName("默认值测试")
    class DefaultValueTests {

        @Test
        @DisplayName("enabled 默认应为 false")
        void enabledShouldDefaultToFalse() {
            assertFalse(properties.isEnabled());
        }

        @Test
        @DisplayName("global 默认不应为 null")
        void globalShouldNotBeNull() {
            assertNotNull(properties.getGlobal());
        }

        @Test
        @DisplayName("methods 默认应为空 Map")
        void methodsShouldDefaultToEmptyMap() {
            assertNotNull(properties.getMethods());
            assertTrue(properties.getMethods().isEmpty());
        }
    }

    @Nested
    @DisplayName("determineProcessUrl 测试")
    class DetermineProcessUrlTests {

        @Test
        @DisplayName("processUrl 为 null 时应自动生成")
        void shouldGenerateProcessUrlWhenNull() {
            LoginMethodConfig config = new LoginMethodConfig();
            properties.getMethods().put("phone", config);

            properties.determineProcessUrl();

            assertEquals("/login/phone", config.getProcessUrl());
        }

        @Test
        @DisplayName("processUrl 为空字符串时应自动生成")
        void shouldGenerateProcessUrlWhenEmpty() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setProcessUrl("   ");
            properties.getMethods().put("email", config);

            properties.determineProcessUrl();

            assertEquals("/login/email", config.getProcessUrl());
        }

        @Test
        @DisplayName("processUrl 已设置时不应覆盖")
        void shouldNotOverrideExistingProcessUrl() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setProcessUrl("/custom/login");
            properties.getMethods().put("username", config);

            properties.determineProcessUrl();

            assertEquals("/custom/login", config.getProcessUrl());
        }

        @Test
        @DisplayName("多个登录方式应分别生成 URL")
        void shouldGenerateUrlsForMultipleMethods() {
            LoginMethodConfig phoneConfig = new LoginMethodConfig();
            LoginMethodConfig emailConfig = new LoginMethodConfig();
            properties.getMethods().put("phone", phoneConfig);
            properties.getMethods().put("email", emailConfig);

            properties.determineProcessUrl();

            assertEquals("/login/phone", phoneConfig.getProcessUrl());
            assertEquals("/login/email", emailConfig.getProcessUrl());
        }

        @Test
        @DisplayName("messageSourceHelper 不为 null 时应记录日志")
        void shouldLogWhenMessageSourceHelperNotNull() {
            LoginMethodConfig config = new LoginMethodConfig();
            properties.getMethods().put("phone", config);
            properties.setMessageSourceHelper(messageSourceHelper);

            properties.determineProcessUrl();

            // 验证 processUrl 已生成
            assertEquals("/login/phone", config.getProcessUrl());
        }
    }

    @Nested
    @DisplayName("initParamName 测试")
    class InitParamNameTests {

        @Test
        @DisplayName("paramName 为空时应合并 principal 和 credential 参数")
        void shouldMergeParamsWhenParamNameEmpty() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setPrincipalParamName(List.of("username", "phone"));
            config.setCredentialParamName(List.of("password", "captcha"));
            properties.getMethods().put("phone", config);

            properties.initParamName();

            assertEquals(4, config.getParamName().size());
            assertTrue(config.getParamName().contains("username"));
            assertTrue(config.getParamName().contains("phone"));
            assertTrue(config.getParamName().contains("password"));
            assertTrue(config.getParamName().contains("captcha"));
        }

        @Test
        @DisplayName("合并时应去重重复参数")
        void shouldDeduplicateWhenMerging() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("username", "password"));
            properties.getMethods().put("test", config);

            properties.initParamName();

            assertEquals(2, config.getParamName().size());
            assertEquals(1, config.getParamName().stream().filter(p -> p.equals("username")).count());
        }

        @Test
        @DisplayName("paramName 已设置且包含所有必要参数时应正常通过")
        void shouldPassWhenParamNameContainsAllRequired() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setParamName(List.of("username", "password"));
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            properties.getMethods().put("test", config);

            assertDoesNotThrow(() -> properties.initParamName());
        }

        @Test
        @DisplayName("paramName 缺少必要参数时应抛出异常")
        void shouldThrowExceptionWhenParamNameMissingRequired() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setParamName(List.of("username"));
            config.setPrincipalParamName(List.of("username", "phone"));
            config.setCredentialParamName(List.of("password"));
            properties.getMethods().put("test", config);

            assertThrows(MultiLoginException.class, () -> properties.initParamName());
        }

        @Test
        @DisplayName("缺少 principal 参数时应抛出异常")
        void shouldThrowExceptionWhenMissingPrincipalParam() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setParamName(List.of("password"));
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            properties.getMethods().put("test", config);

            assertThrows(MultiLoginException.class, () -> properties.initParamName());
        }

        @Test
        @DisplayName("缺少 credential 参数时应抛出异常")
        void shouldThrowExceptionWhenMissingCredentialParam() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setParamName(List.of("username"));
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            properties.getMethods().put("test", config);

            assertThrows(MultiLoginException.class, () -> properties.initParamName());
        }

        @Test
        @DisplayName("paramName 为 null 时应合并参数")
        void shouldMergeWhenParamNameNull() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setParamName(null);
            config.setPrincipalParamName(List.of("username"));
            config.setCredentialParamName(List.of("password"));
            properties.getMethods().put("test", config);

            properties.initParamName();

            assertEquals(2, config.getParamName().size());
        }
    }

    @Nested
    @DisplayName("setter 方法测试")
    class SetterTests {

        @Test
        @DisplayName("setEnabled 应正确设置值")
        void shouldSetEnabled() {
            properties.setEnabled(true);
            assertTrue(properties.isEnabled());
        }

        @Test
        @DisplayName("setGlobal 应正确设置值")
        void shouldSetGlobal() {
            GlobalConfig globalConfig = new GlobalConfig();
            globalConfig.setRequestClientHeader("X-Custom-Header");
            properties.setGlobal(globalConfig);

            assertEquals("X-Custom-Header", properties.getGlobal().getRequestClientHeader());
        }

        @Test
        @DisplayName("setMethods 应正确设置值")
        void shouldSetMethods() {
            LoginMethodConfig config = new LoginMethodConfig();
            properties.setMethods(Map.of("phone", config));

            assertEquals(1, properties.getMethods().size());
            assertTrue(properties.getMethods().containsKey("phone"));
        }

        @Test
        @DisplayName("setMessageSourceHelper 应正确设置值")
        void shouldSetMessageSourceHelper() {
            properties.setMessageSourceHelper(messageSourceHelper);
            assertEquals(messageSourceHelper, properties.getMessageSourceHelper());
        }
    }

    @Nested
    @DisplayName("getParams 私有方法测试")
    class GetParamsTests {

        @Test
        @DisplayName("所有参数都存在时应返回空列表")
        void shouldReturnEmptyListWhenAllParamsPresent() {
            LoginMethodConfig config = new LoginMethodConfig();
            config.setParamName(List.of("username", "password", "phone"));
            config.setPrincipalParamName(List.of("username", "phone"));
            config.setCredentialParamName(List.of("password"));
            properties.getMethods().put("test", config);

            assertDoesNotThrow(() -> properties.initParamName());
        }
    }

    @Nested
    @DisplayName("空 methods 测试")
    class EmptyMethodsTests {

        @Test
        @DisplayName("methods 为空时 determineProcessUrl 应正常执行")
        void shouldHandleEmptyMethodsForDetermineProcessUrl() {
            assertDoesNotThrow(() -> properties.determineProcessUrl());
        }

        @Test
        @DisplayName("methods 为空时 initParamName 应正常执行")
        void shouldHandleEmptyMethodsForInitParamName() {
            assertDoesNotThrow(() -> properties.initParamName());
        }
    }
}