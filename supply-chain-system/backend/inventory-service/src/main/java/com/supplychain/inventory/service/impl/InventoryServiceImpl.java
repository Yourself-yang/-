package com.supplychain.inventory.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.inventory.dto.*;
import com.supplychain.inventory.entity.Inventory;
import com.supplychain.inventory.entity.InventoryRecord;
import com.supplychain.inventory.mapper.InventoryMapper;
import com.supplychain.inventory.mapper.InventoryRecordMapper;
import com.supplychain.inventory.service.InventoryService;
import com.supplychain.inventory.vo.AlertVO;
import com.supplychain.inventory.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<InventoryVO> page(InventoryQueryDTO queryDTO) {
        Page<Inventory> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryDTO.getWarehouse())) {
            wrapper.eq(Inventory::getWarehouse, queryDTO.getWarehouse());
        }
        if (Boolean.TRUE.equals(queryDTO.getLowStock())) {
            wrapper.apply("quantity < safety_stock");
        }
        wrapper.orderByDesc(Inventory::getUpdateTime);

        IPage<Inventory> inventoryPage = inventoryMapper.selectPage(page, wrapper);

        List<InventoryVO> voList = inventoryPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        fillProductInfo(voList);

        IPage<InventoryVO> result = new Page<>(inventoryPage.getCurrent(), inventoryPage.getSize(),
                inventoryPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public Inventory getByProductId(Long productId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getProductId, productId);
        return inventoryMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer changeStock(StockChangeDTO dto, Long operatorId) {
        Inventory inventory = getByProductId(dto.getProductId());
        if (inventory == null) {
            throw new BusinessException("商品库存不存在");
        }

        int beforeQuantity = inventory.getQuantity();
        int afterQuantity;

        if ("OUT".equals(dto.getType())) {
            if (beforeQuantity < dto.getQuantity()) {
                throw new BusinessException("库存不足");
            }
            afterQuantity = beforeQuantity - dto.getQuantity();
        } else if ("IN".equals(dto.getType())) {
            afterQuantity = beforeQuantity + dto.getQuantity();
        } else {
            throw new BusinessException("不支持的变动类型: " + dto.getType());
        }

        inventory.setQuantity(afterQuantity);
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inventory);

        InventoryRecord record = new InventoryRecord();
        record.setProductId(dto.getProductId());
        record.setType(dto.getType());
        record.setBizType(dto.getBizType());
        record.setQuantity(dto.getQuantity());
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setRefOrderNo(dto.getRefOrderNo());
        record.setRemark(dto.getRemark());
        record.setOperatorId(operatorId);
        record.setCreateTime(LocalDateTime.now());
        inventoryRecordMapper.insert(record);

        return afterQuantity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(StockTransferDTO dto, Long operatorId) {
        String refOrderNo = "TRANSFER" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        StockChangeDTO outDto = new StockChangeDTO();
        outDto.setProductId(dto.getProductId());
        outDto.setType("OUT");
        outDto.setBizType("TRANSFER");
        outDto.setQuantity(dto.getQuantity());
        outDto.setRefOrderNo(refOrderNo);
        outDto.setRemark("调拨出库: " + dto.getFromWarehouse() + " -> " + dto.getToWarehouse()
                + (StrUtil.isNotBlank(dto.getRemark()) ? " " + dto.getRemark() : ""));
        changeStock(outDto, operatorId);

        StockChangeDTO inDto = new StockChangeDTO();
        inDto.setProductId(dto.getProductId());
        inDto.setType("IN");
        inDto.setBizType("TRANSFER");
        inDto.setQuantity(dto.getQuantity());
        inDto.setRefOrderNo(refOrderNo);
        inDto.setRemark("调拨入库: " + dto.getFromWarehouse() + " -> " + dto.getToWarehouse()
                + (StrUtil.isNotBlank(dto.getRemark()) ? " " + dto.getRemark() : ""));
        changeStock(inDto, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void check(StockCheckDTO dto, Long operatorId) {
        Inventory inventory = getByProductId(dto.getProductId());
        if (inventory == null) {
            throw new BusinessException("商品库存不存在");
        }

        int currentQuantity = inventory.getQuantity();
        int actualQuantity = dto.getActualQuantity();

        if (actualQuantity > currentQuantity) {
            StockChangeDTO inDto = new StockChangeDTO();
            inDto.setProductId(dto.getProductId());
            inDto.setType("IN");
            inDto.setBizType("CHECK");
            inDto.setQuantity(actualQuantity - currentQuantity);
            inDto.setRemark("盘点盘盈: " + (StrUtil.isNotBlank(dto.getRemark()) ? dto.getRemark() : ""));
            changeStock(inDto, operatorId);
        } else if (actualQuantity < currentQuantity) {
            StockChangeDTO outDto = new StockChangeDTO();
            outDto.setProductId(dto.getProductId());
            outDto.setType("OUT");
            outDto.setBizType("CHECK");
            outDto.setQuantity(currentQuantity - actualQuantity);
            outDto.setRemark("盘点盘亏: " + (StrUtil.isNotBlank(dto.getRemark()) ? dto.getRemark() : ""));
            changeStock(outDto, operatorId);
        }
    }

    @Override
    public List<AlertVO> alerts() {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("quantity < safety_stock");
        wrapper.orderByAsc(Inventory::getQuantity);
        List<Inventory> inventories = inventoryMapper.selectList(wrapper);

        List<InventoryVO> voList = inventories.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        fillProductInfo(voList);

        return voList.stream().map(vo -> {
            AlertVO alert = new AlertVO();
            alert.setProductId(vo.getProductId());
            alert.setProductCode(vo.getProductCode());
            alert.setProductName(vo.getProductName());
            alert.setQuantity(vo.getQuantity());
            alert.setSafetyStock(vo.getSafetyStock());
            alert.setWarehouse(vo.getWarehouse());
            alert.setMessage("商品【" + (StrUtil.isNotBlank(vo.getProductName()) ? vo.getProductName() : vo.getProductId())
                    + "】库存不足，当前库存:" + vo.getQuantity() + "，安全库存:" + vo.getSafetyStock());
            return alert;
        }).collect(Collectors.toList());
    }

    private InventoryVO toVO(Inventory inventory) {
        InventoryVO vo = new InventoryVO();
        vo.setId(inventory.getId());
        vo.setProductId(inventory.getProductId());
        vo.setQuantity(inventory.getQuantity());
        vo.setSafetyStock(inventory.getSafetyStock());
        vo.setWarehouse(inventory.getWarehouse());
        vo.setUpdateTime(inventory.getUpdateTime());
        return vo;
    }

    private void fillProductInfo(List<InventoryVO> voList) {
        if (voList.isEmpty()) {
            return;
        }
        List<Long> productIds = voList.stream()
                .map(InventoryVO::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return;
        }

        String placeholders = productIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, product_name, product_code FROM product WHERE id IN (" + placeholders
                + ") AND deleted = 0";

        List<Map<String, Object>> productList = jdbcTemplate.queryForList(sql, productIds.toArray());

        Map<Long, Map<String, Object>> productMap = productList.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("id")).longValue(),
                        m -> m));

        for (InventoryVO vo : voList) {
            Map<String, Object> p = productMap.get(vo.getProductId());
            if (p != null) {
                vo.setProductName((String) p.get("product_name"));
                vo.setProductCode((String) p.get("product_code"));
            }
        }
    }
}
