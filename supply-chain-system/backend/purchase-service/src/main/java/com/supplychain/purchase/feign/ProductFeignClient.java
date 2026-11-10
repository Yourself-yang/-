package com.supplychain.purchase.feign;

import com.supplychain.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(url = "${service-urls.product}", name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/api/v1/products/{id}")
    Result<Map> getById(@PathVariable Long id);
}
