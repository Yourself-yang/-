package com.supplychain.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "inventory-service", url = "${service-urls.inventory.url}")
public interface InventoryFeignClient {

    @PostMapping("/api/v1/inventory/change")
    Object changeStock(@RequestBody Map<String, Object> body);
}
