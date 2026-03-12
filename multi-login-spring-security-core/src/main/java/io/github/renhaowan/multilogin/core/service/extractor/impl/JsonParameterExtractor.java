package io.github.renhaowan.multilogin.core.service.extractor.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.renhaowan.multilogin.core.i18n.CoreMessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.service.extractor.AbstractInlineParameterExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JSON 参数提取器
 * 从 HTTP 请求的 JSON 请求体中提取配置指定的参数
 *
 * @author wan
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class JsonParameterExtractor extends AbstractInlineParameterExtractor implements ParameterExtractor {

    private final ObjectMapper objectMapper;
    private final MessageSourceHelper messageSourceHelper;

    /**
     * 从请求中提取所有参数
     *
     * @param request HTTP 请求对象
     * @param paramNames 需要提取的参数名集合
     * @return 参数键值对 Map
     */
    @Override
    protected Map<String, Object> doExtractParameters(HttpServletRequest request, Set<String> paramNames) {
        if (!isJsonRequest(request)) {
            final String warningMsg = messageSourceHelper.getMessage(
                CoreMessageCodes.WARN_CONTENT_TYPE_NOT_JSON,
                request.getContentType()
            );
            log.warn(warningMsg);
            return Collections.emptyMap();
        }

        final CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        final Map<String, Object> params = new HashMap<>();
        
        try {
            final Map<String, Object> jsonBody = objectMapper.readValue(
                    cachedRequest.getInputStream(),
                    new TypeReference<>() {}
            );

            for (String paramName : paramNames) {
                if (jsonBody.containsKey(paramName)) {
                    final Object value = jsonBody.get(paramName);
                    if (value != null) {
                        params.put(paramName, value);
                    }
                }
            }
        } catch (Exception e) {
            for (String paramName : paramNames) {
                final String value = cachedRequest.getParameter(paramName);
                if (value != null) {
                    params.put(paramName, value);
                }
            }
            final String warningMsg = messageSourceHelper.getMessage(CoreMessageCodes.WARN_FALLBACK_QUERY_PARAMS);
            log.warn(warningMsg, e);
        }

        return params;
    }

    /**
     * 校验 Content-Type 是否为 JSON
     *
     * @param request HTTP 请求对象
     * @return 是否为 JSON 请求
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return StringUtils.hasText(contentType) &&
                (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE) ||
                        contentType.startsWith("application/vnd.api+json"));
    }

    // 包装HttpServletRequest，缓存请求体（解决输入流只能读一次的问题）
    static class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) {
            super(request);
            try {
                this.cachedBody = cacheInputStream(request.getInputStream());
            } catch (IOException e) {
                this.cachedBody = new byte[0];
            }
        }

        private byte[] cacheInputStream(InputStream inputStream) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ByteArrayInputStream bis = new ByteArrayInputStream(cachedBody);
            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return bis.read();
                }

                @Override
                public boolean isFinished() {
                    return bis.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // 同步读取场景下够用
                }
            };
        }
    }
}
