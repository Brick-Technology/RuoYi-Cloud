package com.ruoyi.common.loadbalance.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class FeignRequestInterceptor implements RequestInterceptor
{
    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (null != attributes) {
            HttpServletRequest request = attributes.getRequest();
            if (null != request) {
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String values = request.getHeader(name);
                        // 跳过 content-length,防止报错Feign报错feign.RetryableException: too many bytes written executing
                        if (name.equals("content-length")) {
                            continue;
                        }
                        requestTemplate.header(name, values);
                    }
                }
            }
        }
    }
}