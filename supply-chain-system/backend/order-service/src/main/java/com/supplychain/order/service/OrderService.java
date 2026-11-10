package com.supplychain.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.order.dto.OrderCreateDTO;
import com.supplychain.order.dto.OrderQueryDTO;
import com.supplychain.order.dto.ShipDTO;
import com.supplychain.order.entity.Order;
import com.supplychain.order.vo.OrderVO;
import com.supplychain.order.vo.StatVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {

    IPage<OrderVO> page(OrderQueryDTO queryDTO);

    Order create(OrderCreateDTO createDTO, Long operatorId);

    void pay(Long id);

    void ship(Long id, ShipDTO shipDTO);

    void confirmReceive(Long id);

    void cancel(Long id);

    void refund(Long id);

    OrderVO getById(Long id);

    StatVO stat();

    List<Map<String, Object>> getRecentSales(int days);

    List<Map<String, Object>> listAllCompleted(LocalDate startDate, LocalDate endDate);
}
