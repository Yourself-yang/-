package com.supplychain.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ai-service", url = "${service-urls.ai.url}")
public interface AiFeignClient {

    @PostMapping("/api/v1/ai/prediction/run")
    Object triggerPrediction();
}
