package io.github.renhaowan.multilogin.core;

import io.github.renhaowan.multilogin.core.properties.config.LoginMethodConfig;
import io.github.renhaowan.multilogin.core.service.extractor.ClientTypeExtractor;
import io.github.renhaowan.multilogin.core.service.extractor.ParameterExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Map;

/**
 * 动态认证过滤器
 * 
 * <p>根据配置动态拦截登录请求，提取参数并委托给认证管理器处理</p>
 * 
 * @author wan
 */
@Slf4j
public class DynamicAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final LoginMethodConfig config;
    private final ParameterExtractor parameterExtractor;
    private final ClientTypeExtractor clientTypeExtractor;
    @Getter
    private final AntPathRequestMatcher antPathRequestMatcher;

    public DynamicAuthenticationFilter(LoginMethodConfig config, ParameterExtractor parameterExtractor, ClientTypeExtractor clientTypeExtractor, AuthenticationManager authenticationManager) {
        // 设置 Filter 拦截路径
        super(new AntPathRequestMatcher(config.getProcessUrl(), config.getHttpMethod()));
        this.antPathRequestMatcher = new AntPathRequestMatcher(config.getProcessUrl(), config.getHttpMethod());
        this.config = config;
        this.parameterExtractor = parameterExtractor;
        this.clientTypeExtractor = clientTypeExtractor;
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // 提取所有参数
        Map<String, Object> allParams = parameterExtractor.extractParameters(request);

        // 提取客户端类型
        String clientType = clientTypeExtractor.extractClientType(request);

        // 创建 Token 实例
        BaseMultiLoginToken token = new BaseMultiLoginToken(
                allParams, clientType, config.getPrincipalParamName(), config.getCredentialParamName()
        );

        // 设置“认证请求”的附加信息，看UsernamePasswordAuthenticationFilter的setDetail方法就可以得到，设置session和ip
        token.setDetails(this.authenticationDetailsSource.buildDetails(request));

        // 委托给 AuthenticationManager (其中包含 Router Provider)
        return this.getAuthenticationManager().authenticate(token);
    }

}
