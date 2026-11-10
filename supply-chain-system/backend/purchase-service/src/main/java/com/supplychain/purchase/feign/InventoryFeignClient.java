package com.supplychain.purchase.feign;

import com.supplychain.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(url = "${service-urls.inventory}", name = "inventory-service")
public interface InventoryFeignClient {

    @PostMapping("/api/v1/inventory/change")
    Result<Map> change(@RequestBody Map body);
}
