package io.github.renhaowan.multilogin.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 多登录方式认证令牌
 * 
 * <p>封装认证所需的所有参数、客户端类型和认证结果</p>
 * 
 * @author wan
 * @since 0.0.1
 */
@Slf4j
@Getter
@Setter
public class BaseMultiLoginToken extends AbstractAuthenticationToken {
    
    /**
     * 所有请求参数
     */
    private final Map<String, Object> allParams;
    
    /**
     * 客户端类型
     */
    private final String clientType;
    
    /**
     * 主体参数名列表
     */
    private final List<String> principalParamName;
    
    /**
     * 凭证参数名列表
     */
    private final List<String> credentialParamName;
    
    /**
     * 认证主体详情
     */
    private Object principalDetails;
    
    /**
     * 创建未认证的令牌
     * 
     * @param allParams 所有请求参数
     * @param clientType 客户端类型
     * @param principalParamName 主体参数名列表
     * @param credentialParamName 凭证参数名列表
     */
    public BaseMultiLoginToken(Map<String, Object> allParams, 
                               String clientType,
                               List<String> principalParamName,
                               List<String> credentialParamName) {
        super(Collections.emptyList());
        this.allParams = allParams;
        this.clientType = clientType;
        this.principalParamName = principalParamName;
        this.credentialParamName = credentialParamName;
        setAuthenticated(false);
    }
    
    /**
     * 创建已认证的令牌
     * 
     * @param allParams 所有请求参数
     * @param clientType 客户端类型
     * @param principalParamName 主体参数名列表
     * @param credentialParamName 凭证参数名列表
     * @param principalDetails 认证主体详情
     * @param authorities 权限列表
     */
    public BaseMultiLoginToken(Map<String, Object> allParams,
                               String clientType,
                               List<String> principalParamName,
                               List<String> credentialParamName,
                               Object principalDetails,
                               Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.allParams = allParams;
        this.clientType = clientType;
        this.principalParamName = principalParamName;
        this.credentialParamName = credentialParamName;
        this.principalDetails = principalDetails;
        setAuthenticated(true);
    }
    
    /**
     * 设置认证主体详情并标记为已认证
     * 
     * @param principalDetails 认证主体详情
     */
    public void setPrincipalDetails(Object principalDetails) {
        this.principalDetails = principalDetails;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return allParams;
    }
    
    @Override
    public Object getPrincipal() {
        return principalDetails != null ? principalDetails : allParams;
    }
}
