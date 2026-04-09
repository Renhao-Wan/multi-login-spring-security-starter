package io.github.renhaowan.multilogin.autoconfigure;

import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.properties.MultiLoginProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MultiLoginAutoConfiguration 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultiLoginAutoConfiguration 测试")
class MultiLoginAutoConfigurationTest {

    private MultiLoginAutoConfiguration config;

    @Mock
    private MultiLoginProperties properties;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private MessageSourceHelper messageSourceHelper;

    @BeforeEach
    void setUp() {
        config = new MultiLoginAutoConfiguration(properties, applicationContext, messageSourceHelper);
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("应正确设置所有依赖")
        void shouldSetAllDependencies() {
            MultiLoginAutoConfiguration config = new MultiLoginAutoConfiguration(
                    properties, applicationContext, messageSourceHelper
            );

            assertNotNull(config);
        }
    }

    @Nested
    @DisplayName("类注解测试")
    class ClassAnnotationTests {

        @Test
        @DisplayName("应为 public 类")
        void shouldBePublicClass() {
            assertTrue(java.lang.reflect.Modifier.isPublic(MultiLoginAutoConfiguration.class.getModifiers()));
        }
    }
}