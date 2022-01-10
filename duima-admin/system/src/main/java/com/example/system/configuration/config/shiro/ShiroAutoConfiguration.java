package com.example.system.configuration.config.shiro;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:
 */
@Configuration
@EnableConfigurationProperties(value = {ShiroProperties.class})
public class ShiroAutoConfiguration {

    private ShiroProperties shiroProperties;


    public static final String SHIRO_FILTER_NAME = "shiroFilter";

    public ShiroAutoConfiguration(ShiroProperties shiroProperties){
        this.shiroProperties = shiroProperties;
    }


    /**
     * 创建凭证匹配器
     */
    @Bean
    public HashedCredentialsMatcher getHashedCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        //注入散列算法名
        matcher.setHashAlgorithmName(shiroProperties.getHashAlgorithmName());
        //注入散列次数
        matcher.setHashIterations(shiroProperties.getHashIterations());
        return matcher;
    }

    /**
     * 创建自定义realm
     * 并注入凭证匹配器
     */
    @Bean
    @ConditionalOnClass(value = {UserRealm.class})
    public UserRealm getUserRealm(HashedCredentialsMatcher matcher) {
        UserRealm userRealm = new UserRealm();
        //注入凭证匹配器
        userRealm.setCredentialsMatcher(matcher);
        return userRealm;
    }

    /**
     * 创建安全管理器
     */
    @Bean
    @ConditionalOnClass(value = DefaultWebSecurityManager.class)
    public DefaultWebSecurityManager getSecurityManager(DefaultWebSessionManager defaultWebSessionManager, UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //注入realm
        securityManager.setRealm(userRealm);
        securityManager.setSessionManager(defaultWebSessionManager);
        return securityManager;
    }

    /**
     * -声明过滤器
     * Shiro 的Web过滤器 id必须和web.xml里面的shiroFilter的 targetBeanName的值一样
     */
    @Bean(value = SHIRO_FILTER_NAME)
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        //注入安全管理器
        bean.setSecurityManager(securityManager);
        //处理用户未认证访问要认证的地址的跳转问题   默认是跳转到shiroProperties.getLoginUrl()现在改成以json串形式返回
        Map<String, Filter> filters = new HashMap<>();
        filters.put("authc", new ShiroLoginFilter());
        bean.setFilters(filters);

        Map<String, String> map = new HashMap<>();
        //配置不拦击的路径
        String[] anonUrls = shiroProperties.getAnonUrls();
        if (anonUrls != null && anonUrls.length > 0) {
            for (String anonUrl : anonUrls) {
                map.put(anonUrl, "anon");
            }
        }
        //配置拦截的路径
        String[] authcUrls = this.shiroProperties.getAuthcUrls();
        if (authcUrls != null && authcUrls.length > 0) {
            for (String authcUrl : authcUrls) {
                map.put(authcUrl, "authc");
            }
        }
        bean.setFilterChainDefinitionMap(map);
        return bean;
    }


    /**
     * 注册DelegatingFilterProxy
     */
    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> registDelegatingFilterProxy() {
        //创建注册器
        FilterRegistrationBean<DelegatingFilterProxy> bean = new FilterRegistrationBean<>();
        //创建过滤器
        DelegatingFilterProxy proxy = new DelegatingFilterProxy();
        //注入过滤器
        bean.setFilter(proxy);
        proxy.setTargetFilterLifecycle(true);
        proxy.setTargetBeanName(SHIRO_FILTER_NAME);
        Collection<String> servleNames = new ArrayList<>();
        servleNames.add(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
        bean.setServletNames(servleNames);
        return bean;
    }


    /**
     * @see DefaultWebSessionManager
     * @return
     */
    @Bean
    public DefaultWebSessionManager defaultWebSessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(1800000);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionIdCookie(getSessionIdCookie());
        return sessionManager;
    }

    /**
     * 给shiro的sessionId默认的JSSESSIONID名字改掉
     * @return
     */
    @Bean
    public SimpleCookie getSessionIdCookie(){
        SimpleCookie simpleCookie = new SimpleCookie("webcookie");
        /**
         * HttpOnly标志的引入是为了防止设置了该标志的cookie被JavaScript读取，
         * 但事实证明设置了这种cookie在某些浏览器中却能被JavaScript覆盖，
         * 可被攻击者利用来发动session fixation攻击
         */
        simpleCookie.setHttpOnly(true);
        /**
         * 设置浏览器cookie过期时间，如果不设置默认为-1，表示关闭浏览器即过期
         * cookie的单位为秒 比如60*60为1小时
         */
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    /*加入注解的使用，不加入这个注解不生效--开始*/

    /**
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }
    /*加入注解的使用，不加入这个注解不生效--结束*/

}
