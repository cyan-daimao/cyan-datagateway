package com.cyan.datagateway.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "cyan-datagateway", path = "/rpc/v1/datagateway")
public interface DatagatewayClient {
}
