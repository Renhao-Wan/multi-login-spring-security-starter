package io.github.renhaowan.multilogin.core;

import io.github.renhaowan.multilogin.core.exception.MultiLoginException;
import io.github.renhaowan.multilogin.core.i18n.CoreMessageCodes;
import io.github.renhaowan.multilogin.core.i18n.MessageSourceHelper;
import io.github.renhaowan.multilogin.core.service.BusinessAuthenticationLogic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由认证提供者
 * 
 * <p>根据客户端类型路由到对应的业务认证逻辑</p>
 * 
 * @author wan
 */
@Slf4j
public class RouterAuthenticationProvider implements AuthenticationProvider {
    
    private final Map<String, BusinessAuthenticationLogic> businessProviders;
    private final MessageSourceHelper messageSourceHelper;

    public RouterAuthenticationProvider(List<BusinessAuthenticationLogic> providers, 
                                       List<String> clientTypes,
                                       MessageSourceHelper messageSourceHelper) {
        this.businessProviders = new HashMap<>();
        this.messageSourceHelper = messageSourceHelper;
        for (int i = 0; i < clientTypes.size(); i++) {
            this.businessProviders.put(clientTypes.get(i), providers.get(i));
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof BaseMultiLoginToken token)) {
            return null;
        }

        final String clientType = token.getClientType();
        final BusinessAuthenticationLogic businessLogic = businessProviders.get(clientType);

        if (businessLogic == null) {
            final String errorMsg = messageSourceHelper.getMessage(
                CoreMessageCodes.ERROR_BUSINESS_LOGIC_NOT_FOUND,
                clientType
            );
            log.error(errorMsg);
            throw new MultiLoginException(
                CoreMessageCodes.ERROR_CLIENT_TYPE_UNKNOWN,
                clientType
            );
        }

        final Object principal = businessLogic.authenticate(token.getAllParams());

        if (principal == null) {
            final String errorMsg = messageSourceHelper.getMessage(CoreMessageCodes.ERROR_PRINCIPAL_IS_NULL);
            log.error(errorMsg);
            throw new MultiLoginException(CoreMessageCodes.ERROR_AUTHENTICATION_FAILED);
        }

        token.setPrincipalDetails(principal);
        if (log.isDebugEnabled()) {
            final String debugMsg = messageSourceHelper.getMessage(
                CoreMessageCodes.DEBUG_AUTHENTICATION_SUCCESS,
                clientType
            );
            log.debug(debugMsg);
        }
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BaseMultiLoginToken.class.isAssignableFrom(authentication);
    }
}
