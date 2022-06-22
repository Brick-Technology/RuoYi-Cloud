package com.ruoyi.common.loadbalance.core;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import org.springframework.cloud.client.ServiceInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NacosBalancer extends Balancer {

    /**
     * Choose instance by weight.
     * @param instances Instance List
     * @return the chosen instance
     */
    public static Instance getHostByRandomWeight2(List<Instance> instances) {
        return getHostByRandomWeight(instances);
    }

    /**
     * Spring Cloud LoadBalancer Choose instance by weight.
     * @param serviceInstances Instance List
     * @return the chosen instance
     */
    public static ServiceInstance getHostByRandomWeight3(
            List<ServiceInstance> serviceInstances) {
        Map<Instance, ServiceInstance> instanceMap = new HashMap<>();
        List<Instance> nacosInstance = serviceInstances.stream().map(serviceInstance -> {
            Map<String, String> metadata = serviceInstance.getMetadata();

            // see
            // com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery.hostToServiceInstance()
            Instance instance = new Instance();
            instance.setIp(serviceInstance.getHost());
            instance.setPort(serviceInstance.getPort());
            instance.setWeight(Double.parseDouble(metadata.get("nacos.weight")));
            instance.setHealthy(Boolean.parseBoolean(metadata.get("nacos.healthy")));
            instanceMap.put(instance, serviceInstance);
            return instance;
        }).collect(Collectors.toList());

        Instance instance = getHostByRandomWeight2(nacosInstance);
        return instanceMap.get(instance);
    }

}
