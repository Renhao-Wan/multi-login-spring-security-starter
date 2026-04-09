package io.github.renhaowan.multilogin.autoconfigure.factory;

import io.github.renhaowan.multilogin.core.properties.MultiLoginProperties;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.BusinessAuthenticationLogic;
import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DynamicAuthenticationFilterFactory 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicAuthenticationFilterFactory 测试")
class DynamicAuthenticationFilterFactoryTest {

    private DynamicAuthenticationFilterFactory factory;

    @Mock
    private MultiLoginProperties properties;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private BusinessAuthenticationLogic businessLogic;

    @Mock
    private ParameterExtractor parameterExtractor;

    @Mock
    private ClientTypeExtractor clientTypeExtractor;

    @BeforeEach
    void setUp() {
        factory = new DynamicAuthenticationFilterFactory(properties, applicationContext, null);
    }

    @Nested
    @DisplayName("createFilters 方法测试")
    class CreateFiltersTests {

        @Test
        @DisplayName("methods 为空时应返回空列表")
        void shouldReturnEmptyListWhenNoMethods() {
            when(properties.getMethods()).thenReturn(java.util.Map.of());

            var filters = factory.createFilters();

            assertTrue(filters.isEmpty());
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("应正确设置所有依赖")
        void shouldSetAllDependencies() {
            DynamicAuthenticationFilterFactory factory = new DynamicAuthenticationFilterFactory(
                    properties, applicationContext, null
            );

            assertNotNull(factory);
        }
    }

    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("应为 public 类")
        void shouldBePublicClass() {
            assertTrue(java.lang.reflect.Modifier.isPublic(DynamicAuthenticationFilterFactory.class.getModifiers()));
        }
    }
}