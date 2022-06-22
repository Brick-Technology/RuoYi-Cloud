package com.ruoyi.common.loadbalance.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@LoadBalancerClients(defaultConfiguration = CustomLoadBalanceClientConfiguration.class)
public class CustomLoadBalanceAutoConfiguration {

}
