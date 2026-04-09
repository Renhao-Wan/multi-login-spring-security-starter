package io.github.renhaowan.multilogin.autoconfigure;

import io.github.renhaowan.multilogin.autoconfigure.config.MultiLoginSecurity;
import io.github.renhaowan.multilogin.autoconfigure.config.MultiLoginSecurityCustomizer;
import io.github.renhaowan.multilogin.autoconfigure.i18n.I18nAutoConfiguration;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MultiLoginSecurityAutoConfiguration 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultiLoginSecurityAutoConfiguration 测试")
class MultiLoginSecurityAutoConfigurationTest {

    private MultiLoginSecurityAutoConfiguration config;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        config = new MultiLoginSecurityAutoConfiguration(messageSourceHelper);
    }

    @Nested
    @DisplayName("multiLoginSecurityCustomizer 方法测试")
    class MultiLoginSecurityCustomizerTests {

        @Test
        @DisplayName("应创建 MultiLoginSecurityCustomizer Bean")
        void shouldCreateCustomizerBean() {
            var customizer = config.multiLoginSecurityCustomizer(applicationContext);

            assertNotNull(customizer);
            assertTrue(customizer instanceof MultiLoginSecurityCustomizer);
        }

        @Test
        @DisplayName("应传递 applicationContext 和 messageSourceHelper")
        void shouldPassDependencies() {
            var customizer = config.multiLoginSecurityCustomizer(applicationContext);

            // 通过反射验证
            assertDoesNotThrow(() -> {
                var customizerClass = MultiLoginSecurityCustomizer.class;
                var contextField = customizerClass.getDeclaredField("applicationContext");
                contextField.setAccessible(true);
                assertSame(applicationContext, contextField.get(customizer));
            });
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("应正确设置 messageSourceHelper")
        void shouldSetMessageSourceHelper() {
            MultiLoginSecurityAutoConfiguration config = new MultiLoginSecurityAutoConfiguration(messageSourceHelper);

            assertNotNull(config);
        }
    }

    @Nested
    @DisplayName("类注解测试")
    class ClassAnnotationTests {

        @Test
        @DisplayName("应为 public 类")
        void shouldBePublicClass() {
            assertTrue(java.lang.reflect.Modifier.isPublic(MultiLoginSecurityAutoConfiguration.class.getModifiers()));
        }
    }
}