package io.github.renhaowan.multilogin.autoconfigure.config;

import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MultiLoginSecurityCustomizer 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultiLoginSecurityCustomizer 测试")
class MultiLoginSecurityCustomizerTest {

    private MultiLoginSecurityCustomizer customizer;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @Mock
    private HttpSecurity http;

    @BeforeEach
    void setUp() {
        customizer = new MultiLoginSecurityCustomizer(applicationContext, messageSourceHelper);
    }

    @Nested
    @DisplayName("init 方法测试")
    class InitTests {

        @Test
        @DisplayName("过滤器为空时应正常返回")
        void shouldReturnWhenNoFilters() throws Exception {
            when(applicationContext.getBean("multiLoginFilters")).thenThrow(new RuntimeException("Bean not found"));

            assertDoesNotThrow(() -> customizer.init(http));
        }
    }

    @Nested
    @DisplayName("configure 方法测试")
    class ConfigureTests {

        @Test
        @DisplayName("过滤器为空时应正常返回")
        void shouldReturnWhenNoFilters() throws Exception {
            when(applicationContext.getBean("multiLoginFilters")).thenThrow(new RuntimeException("Bean not found"));

            assertDoesNotThrow(() -> customizer.configure(http));
        }
    }

    @Nested
    @DisplayName("getMultiLoginFilters 方法测试")
    class GetMultiLoginFiltersTests {

        @Test
        @DisplayName("Bean 存在时应返回过滤器列表")
        void shouldReturnFiltersWhenBeanExists() {
            java.util.List<?> filters = java.util.List.of();
            when(applicationContext.getBean("multiLoginFilters")).thenReturn(filters);

            var result = invokeGetMultiLoginFilters();

            assertNotNull(result);
        }

        @Test
        @DisplayName("Bean 不存在时应返回空列表")
        void shouldReturnEmptyListWhenBeanNotFound() {
            when(applicationContext.getBean("multiLoginFilters")).thenThrow(new RuntimeException("Bean not found"));

            var result = invokeGetMultiLoginFilters();

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("应继承自 AbstractHttpConfigurer")
        void shouldBeAbstractHttpConfigurer() {
            assertTrue(customizer instanceof org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer);
        }
    }

    /**
     * 通过反射调用私有方法
     */
    private java.util.List<?> invokeGetMultiLoginFilters() {
        try {
            var method = MultiLoginSecurityCustomizer.class.getDeclaredMethod("getMultiLoginFilters");
            method.setAccessible(true);
            return (java.util.List<?>) method.invoke(customizer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}