package com.ruoyi.common.loadbalance.core;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.ruoyi.common.loadbalance.common.Constant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class CustomSpringCloudLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;


    public CustomSpringCloudLoadBalancer(
            ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
            String serviceId, NacosDiscoveryProperties nacosDiscoveryProperties) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                .getIfAvailable(NoopServiceInstanceListSupplier::new);
        String tempVersion = null;
        if (request instanceof DefaultRequest) {
            DefaultRequest defaultRequest = (DefaultRequest) request;
            if(defaultRequest.getContext() instanceof RequestDataContext){
                RequestDataContext requestDataContext = (RequestDataContext)defaultRequest.getContext();
                RequestData requestData = requestDataContext.getClientRequest();
                List<String> versionList = requestData.getHeaders().get(Constant.HEADER_KEY_VERSION);
                if(!org.springframework.util.CollectionUtils.isEmpty(versionList)){
                    tempVersion = versionList.get(0);
                }
            }
        }
        final String version = tempVersion;
        return supplier.get().next().map(new Function<List<ServiceInstance>, Response<ServiceInstance>>() {
            @Override
            public Response<ServiceInstance> apply(List<ServiceInstance> serviceInstances) {
                return getInstanceResponse(serviceInstances,version);
            }
        });
    }

    private Response<ServiceInstance> getInstanceResponse(
            List<ServiceInstance> serviceInstances,String version) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        try {
            String clusterName = this.nacosDiscoveryProperties.getClusterName();

            List<ServiceInstance> instancesToChoose = serviceInstances;
            if (StringUtils.isNotBlank(clusterName)) {
                List<ServiceInstance> sameClusterInstances = serviceInstances.stream()
                        .filter(serviceInstance -> {
                            String cluster = serviceInstance.getMetadata()
                                    .get("nacos.cluster");
                            return StringUtils.equals(cluster, clusterName);
                        }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                    instancesToChoose = sameClusterInstances;
                }
            }
            else {
                log.warn(
                        "A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}",
                        serviceId, clusterName, serviceInstances);
            }
            if(!StringUtils.isEmpty(version)){
                //chose version
                List<ServiceInstance> sameVersionInstances = instancesToChoose.stream()
                        .filter(serviceInstance -> {
                            String metadataVersion = serviceInstance.getMetadata()
                                    .get(Constant.HEADER_KEY_VERSION);
                            return StringUtils.equals(metadataVersion, version);
                        }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameVersionInstances)) {
                    instancesToChoose = sameVersionInstances;
                }else{
                    //chose without version
                    List<ServiceInstance> withoutVersionInstances = instancesToChoose.stream()
                            .filter(serviceInstance -> {
                                String metadataVersion = serviceInstance.getMetadata()
                                        .get(Constant.HEADER_KEY_VERSION);
                                return StringUtils.isEmpty(metadataVersion);
                            }).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(withoutVersionInstances)) {
                        instancesToChoose = withoutVersionInstances;
                    }else{
                        log.warn("No servers available for service without version: " + this.serviceId);
                        return new EmptyResponse();
                    }
                }
            }else{
                //chose without version
                List<ServiceInstance> withoutVersionInstances = instancesToChoose.stream()
                        .filter(serviceInstance -> {
                            String metadataVersion = serviceInstance.getMetadata()
                                    .get(Constant.HEADER_KEY_VERSION);
                            return StringUtils.isEmpty(metadataVersion);
                        }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(withoutVersionInstances)) {
                    instancesToChoose = withoutVersionInstances;
                }else{
                    log.warn("No servers available for service without version: " + this.serviceId);
                    return new EmptyResponse();
                }
            }
            ServiceInstance instance = NacosBalancer
                    .getHostByRandomWeight3(instancesToChoose);

            return new DefaultResponse(instance);
        }
        catch (Exception e) {
            log.warn("NacosLoadBalancer error", e);
            return null;
        }

    }

}
