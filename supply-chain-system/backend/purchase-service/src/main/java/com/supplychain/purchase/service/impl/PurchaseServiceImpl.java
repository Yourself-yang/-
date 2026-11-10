package com.supplychain.purchase.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.common.result.Result;
import com.supplychain.purchase.dto.ApproveDTO;
import com.supplychain.purchase.dto.PurchaseCreateDTO;
import com.supplychain.purchase.dto.PurchaseItemDTO;
import com.supplychain.purchase.dto.ReceiveDTO;
import com.supplychain.purchase.dto.ReceiveItemDTO;
import com.supplychain.purchase.entity.PurchaseOrder;
import com.supplychain.purchase.entity.PurchaseOrderItem;
import com.supplychain.purchase.feign.InventoryFeignClient;
import com.supplychain.purchase.mapper.PurchaseOrderItemMapper;
import com.supplychain.purchase.mapper.PurchaseOrderMapper;
import com.supplychain.purchase.service.PurchaseService;
import com.supplychain.purchase.vo.PurchaseItemVO;
import com.supplychain.purchase.vo.PurchaseOrderVO;
import com.supplychain.purchase.vo.PurchaseStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final JdbcTemplate jdbcTemplate;
    private final InventoryFeignClient inventoryFeignClient;

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Override
    public IPage<PurchaseOrderVO> page(String keyword, Long supplierId, String status, Integer pageNum,
            Integer pageSize) {
        Page<PurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(keyword), PurchaseOrder::getOrderNo, keyword)
                .eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId)
                .eq(StrUtil.isNotBlank(status), PurchaseOrder::getStatus, status)
                .orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> orderPage = purchaseOrderMapper.selectPage(page, wrapper);

        IPage<PurchaseOrderVO> voPage = new Page<>(pageNum, pageSize, orderPage.getTotal());
        List<PurchaseOrderVO> voList = new ArrayList<>();
        for (PurchaseOrder order : orderPage.getRecords()) {
            PurchaseOrderVO vo = convertToVO(order);
            String supplierName = getSupplierName(order.getSupplierId());
            vo.setSupplierName(supplierName);
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public PurchaseOrderVO getById(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        PurchaseOrderVO vo = convertToVO(order);
        String supplierName = getSupplierName(order.getSupplierId());
        vo.setSupplierName(supplierName);

        LambdaQueryWrapper<PurchaseOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseOrderItem::getOrderId, id);
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(itemWrapper);
        List<PurchaseItemVO> itemVOs = new ArrayList<>();
        for (PurchaseOrderItem item : items) {
            PurchaseItemVO itemVO = new PurchaseItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder create(PurchaseCreateDTO dto, Long creatorId) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setSupplierId(dto.getSupplierId());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(STATUS_PENDING);
        order.setRemark(dto.getRemark());
        order.setCreatorId(creatorId);
        order.setCreateTime(LocalDateTime.now());
        purchaseOrderMapper.insert(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseItemDTO itemDTO : dto.getItems()) {
            String productName = getProductName(itemDTO.getProductId());
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setProductName(productName);
            item.setQuantity(itemDTO.getQuantity());
            item.setReceivedQuantity(0);
            item.setUnitPrice(itemDTO.getUnitPrice());
            BigDecimal subtotal = itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            item.setSubtotal(subtotal);
            purchaseOrderItemMapper.insert(item);
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        purchaseOrderMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, ApproveDTO dto, Long operatorId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("只有待审批的采购单才能审批");
        }
        order.setStatus(dto.getApproved() ? STATUS_APPROVED : STATUS_REJECTED);
        if (StrUtil.isNotBlank(dto.getRemark())) {
            order.setRemark(dto.getRemark());
        }
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (!STATUS_APPROVED.equals(order.getStatus())) {
            throw new BusinessException("只有已审批的采购单才能发货");
        }
        order.setStatus(STATUS_SHIPPED);
        purchaseOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long id, ReceiveDTO dto) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (!STATUS_SHIPPED.equals(order.getStatus())) {
            throw new BusinessException("只有已发货的采购单才能入库");
        }

        LambdaQueryWrapper<PurchaseOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseOrderItem::getOrderId, id);
        List<PurchaseOrderItem> allItems = purchaseOrderItemMapper.selectList(itemWrapper);
        Map<Long, PurchaseOrderItem> itemMap = new HashMap<>();
        for (PurchaseOrderItem item : allItems) {
            itemMap.put(item.getId(), item);
        }

        for (ReceiveItemDTO receiveItem : dto.getItems()) {
            PurchaseOrderItem item = itemMap.get(receiveItem.getItemId());
            if (item == null) {
                throw new BusinessException("采购明细不存在: " + receiveItem.getItemId());
            }
            int newReceivedQty = item.getReceivedQuantity() + receiveItem.getReceivedQty();
            if (newReceivedQty > item.getQuantity()) {
                throw new BusinessException("入库数量超过采购数量，商品: " + item.getProductName());
            }
            item.setReceivedQuantity(newReceivedQty);
            purchaseOrderItemMapper.updateById(item);

            Map<String, Object> inventoryBody = new HashMap<>();
            inventoryBody.put("productId", item.getProductId());
            inventoryBody.put("changeType", "IN");
            inventoryBody.put("bizType", "PURCHASE");
            inventoryBody.put("quantity", receiveItem.getReceivedQty());
            inventoryBody.put("refOrderNo", order.getOrderNo());
            try {
                Result<Map> result = inventoryFeignClient.change(inventoryBody);
                if (result == null || result.getCode() != 200) {
                    throw new BusinessException("库存服务调用失败: " + (result != null ? result.getMessage() : "无响应"));
                }
            } catch (Exception e) {
                throw new BusinessException("库存服务调用失败: " + e.getMessage());
            }
        }

        boolean allReceived = true;
        for (PurchaseOrderItem item : allItems) {
            PurchaseOrderItem updatedItem = purchaseOrderItemMapper.selectById(item.getId());
            if (updatedItem.getReceivedQuantity() < updatedItem.getQuantity()) {
                allReceived = false;
                break;
            }
        }

        if (allReceived) {
            order.setStatus(STATUS_RECEIVED);
            order.setReceiveTime(LocalDateTime.now());
            purchaseOrderMapper.updateById(order);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if (!STATUS_PENDING.equals(order.getStatus()) && !STATUS_REJECTED.equals(order.getStatus())) {
            throw new BusinessException("只有待审批或已驳回的采购单才能删除");
        }
        LambdaQueryWrapper<PurchaseOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PurchaseOrderItem::getOrderId, id);
        purchaseOrderItemMapper.delete(itemWrapper);
        purchaseOrderMapper.deleteById(id);
    }

    @Override
    public PurchaseStatVO stat() {
        PurchaseStatVO stat = new PurchaseStatVO();
        stat.setPendingCount(countByStatus(STATUS_PENDING));
        stat.setApprovedCount(countByStatus(STATUS_APPROVED));
        stat.setShippedCount(countByStatus(STATUS_SHIPPED));
        stat.setReceivedCount(countByStatus(STATUS_RECEIVED));
        stat.setTotalAmount(getMonthTotalAmount());
        return stat;
    }

    private Long countByStatus(String status) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, status);
        return purchaseOrderMapper.selectCount(wrapper);
    }

    private BigDecimal getMonthTotalAmount() {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseOrder::getStatus, STATUS_RECEIVED)
                .ge(PurchaseOrder::getReceiveTime, start);
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(wrapper);
        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrder order : orders) {
            if (order.getTotalAmount() != null) {
                total = total.add(order.getTotalAmount());
            }
        }
        return total;
    }

    private String getSupplierName(Long supplierId) {
        try {
            String sql = "SELECT supplier_name FROM supplier WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, supplierId);
        } catch (Exception e) {
            return null;
        }
    }

    private String getProductName(Long productId) {
        try {
            String sql = "SELECT product_name FROM product WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, productId);
        } catch (Exception e) {
            throw new BusinessException("商品不存在: " + productId);
        }
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = RandomUtil.randomNumbers(4);
        return "PO" + datePart + randomPart;
    }

    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }
}
