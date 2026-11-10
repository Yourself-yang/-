package com.supplychain.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.common.result.PageResult;
import com.supplychain.common.result.Result;
import com.supplychain.order.dto.OrderCreateDTO;
import com.supplychain.order.dto.OrderQueryDTO;
import com.supplychain.order.dto.ShipDTO;
import com.supplychain.order.entity.Order;
import com.supplychain.order.service.OrderService;
import com.supplychain.order.vo.OrderVO;
import com.supplychain.order.vo.StatVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(OrderQueryDTO queryDTO) {
        IPage<OrderVO> p = orderService.page(queryDTO);
        return Result.success(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> list(OrderQueryDTO queryDTO) {
        return page(queryDTO);
    }

    @GetMapping("/{id:\\d+}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderCreateDTO createDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(orderService.create(createDTO, operatorId));
    }

    @PutMapping("/{id:\\d+}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/ship")
    public Result<Void> ship(@PathVariable Long id, @RequestBody(required = false) ShipDTO shipDTO) {
        orderService.ship(id, shipDTO != null ? shipDTO : new ShipDTO());
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/confirm")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        orderService.confirmReceive(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    @PutMapping("/{id:\\d+}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        orderService.refund(id);
        return Result.success();
    }

    @GetMapping("/{id:\\d+}/track")
    public Result<OrderVO> track(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/stat")
    public Result<StatVO> stat() {
        return Result.success(orderService.stat());
    }

    @GetMapping("/stat/overview")
    public Result<StatVO> statOverview() {
        return stat();
    }

    @GetMapping("/recent-sales")
    public Result<List<Map<String, Object>>> getRecentSales(@RequestParam(defaultValue = "30") int days) {
        return Result.success(orderService.getRecentSales(days));
    }

    @GetMapping("/completed")
    public Result<List<Map<String, Object>>> listAllCompleted(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(orderService.listAllCompleted(startDate, endDate));
    }
}
