package io.github.renhaowan.multilogin.core.exception;

import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MultiLoginException 单元测试
 *
 * @author wan
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MultiLoginException 测试")
class MultiLoginExceptionTest {

    @Mock
    private MessageSourceHelper messageSourceHelper;

    private static final String ERROR_CODE = "error.test";
    private static final String ERROR_MESSAGE = "测试错误消息";

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅使用错误码创建异常")
        void shouldCreateExceptionWithErrorCodeOnly() {
            MultiLoginException exception = new MultiLoginException(ERROR_CODE);

            assertEquals(ERROR_CODE, exception.getErrorCode());
            assertArrayEquals(new Object[0], exception.getErrorArgs());
            assertNotNull(exception.getContext());
            assertTrue(exception.getContext().isEmpty());
            assertEquals(ERROR_CODE, exception.getMessage());
        }

        @Test
        @DisplayName("使用错误码和参数创建异常")
        void shouldCreateExceptionWithErrorCodeAndArgs() {
            Object[] args = {"param1", 123};

            MultiLoginException exception = new MultiLoginException(ERROR_CODE, args);

            assertEquals(ERROR_CODE, exception.getErrorCode());
            assertArrayEquals(args, exception.getErrorArgs());
            assertNotNull(exception.getContext());
        }

        @Test
        @DisplayName("使用错误码和原因创建异常")
        void shouldCreateExceptionWithErrorCodeAndCause() {
            Throwable cause = new RuntimeException("原始异常");

            MultiLoginException exception = new MultiLoginException(ERROR_CODE, cause);

            assertEquals(ERROR_CODE, exception.getErrorCode());
            assertArrayEquals(new Object[0], exception.getErrorArgs());
            assertSame(cause, exception.getCause());
        }

        @Test
        @DisplayName("使用错误码、原因和参数创建异常")
        void shouldCreateExceptionWithAllParameters() {
            Throwable cause = new RuntimeException("原始异常");
            Object[] args = {"arg1", "arg2"};

            MultiLoginException exception = new MultiLoginException(ERROR_CODE, cause, args);

            assertEquals(ERROR_CODE, exception.getErrorCode());
            assertArrayEquals(args, exception.getErrorArgs());
            assertSame(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("上下文管理测试")
    class ContextTests {

        @Test
        @DisplayName("添加单个上下文信息")
        void shouldAddSingleContext() {
            MultiLoginException exception = new MultiLoginException(ERROR_CODE);

            MultiLoginException result = exception.withContext("key1", "value1");

            assertSame(exception, result);
            assertEquals("value1", exception.getContext().get("key1"));
        }

        @Test
        @DisplayName("添加多个上下文信息")
        void shouldAddMultipleContexts() {
            MultiLoginException exception = new MultiLoginException(ERROR_CODE);

            exception.withContext("key1", "value1")
                    .withContext("key2", 123)
                    .withContext("key3", true);

            assertEquals(3, exception.getContext().size());
            assertEquals("value1", exception.getContext().get("key1"));
            assertEquals(123, exception.getContext().get("key2"));
            assertEquals(true, exception.getContext().get("key3"));
        }

        @Test
        @DisplayName("覆盖已存在的上下文信息")
        void shouldOverrideExistingContext() {
            MultiLoginException exception = new MultiLoginException(ERROR_CODE);

            exception.withContext("key", "value1");
            exception.withContext("key", "value2");

            assertEquals("value2", exception.getContext().get("key"));
        }
    }

    @Nested
    @DisplayName("国际化消息测试")
    class LocalizedMessageTests {

        @Test
        @DisplayName("获取本地化错误消息")
        void shouldGetLocalizedMessage() {
            Object[] args = {"param1"};
            MultiLoginException exception = new MultiLoginException(ERROR_CODE, args);

            when(messageSourceHelper.getMessage(ERROR_CODE, args)).thenReturn(ERROR_MESSAGE);

            String result = exception.getLocalizedMessage(messageSourceHelper);

            assertEquals(ERROR_MESSAGE, result);
            verify(messageSourceHelper).getMessage(ERROR_CODE, args);
        }

        @Test
        @DisplayName("无参数时获取本地化消息")
        void shouldGetLocalizedMessageWithoutArgs() {
            MultiLoginException exception = new MultiLoginException(ERROR_CODE);

            when(messageSourceHelper.getMessage(ERROR_CODE, new Object[0])).thenReturn(ERROR_MESSAGE);

            String result = exception.getLocalizedMessage(messageSourceHelper);

            assertEquals(ERROR_MESSAGE, result);
        }
    }

    @Nested
    @DisplayName("异常继承测试")
    class InheritanceTests {

        @Test
        @DisplayName("应继承自 RuntimeException")
        void shouldBeRuntimeException() {
            MultiLoginException exception = new MultiLoginException(ERROR_CODE);

            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("可以被抛出和捕获")
        void canBeThrownAndCaught() {
            assertThrows(MultiLoginException.class, () -> {
                throw new MultiLoginException(ERROR_CODE);
            });
        }
    }
}