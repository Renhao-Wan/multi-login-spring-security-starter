package io.github.renhaowan.multilogin.core.service.extractor.impl;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.CoreMessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.properties.config.GlobalConfig;
import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * 默认客户端类型提取器实现
 * 从请求头中提取客户端类型
 *
 * @author wan
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class HeaderClientTypeExtractor implements ClientTypeExtractor {

    private final MessageSourceHelper messageSourceHelper;

    /**
     * 登录方法配置
     */
    private LoginMethodConfig config;

    /**
     * 全局配置
     */
    private GlobalConfig globalConfig;

    /**
     * 从请求中提取客户端类型
     *
     * @param request HTTP 请求对象
     * @return 客户端类型
     */
    @Override
    public String extractClientType(HttpServletRequest request) {
        final String requestClientHeader = Optional.ofNullable(config.getRequestClientHeader())
                .orElse(globalConfig.getRequestClientHeader());

        final List<String> clientTypes = Optional.ofNullable(config.getClientTypes())
                .orElse(globalConfig.getClientTypes());

        final String clientType = request.getHeader(requestClientHeader);

        if (clientType == null || !clientTypes.contains(clientType)) {
            if (!clientTypes.isEmpty()) {
                final String defaultType = clientTypes.get(0);
                final String warningMsg = messageSourceHelper.getMessage(
                    CoreMessageCodes.WARN_CLIENT_TYPE_DEFAULT,
                    defaultType
                );
                log.warn(warningMsg);
                return defaultType;
            }
            throw new MultiLoginException(CoreMessageCodes.ERROR_CLIENT_TYPE_NOT_DETERMINED);
        }

        return clientType;
    }
}