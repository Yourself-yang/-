package com.supplychain.inventory.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.inventory.entity.InventoryRecord;
import com.supplychain.inventory.mapper.InventoryRecordMapper;
import com.supplychain.inventory.service.InventoryRecordService;
import com.supplychain.inventory.vo.InventoryRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryRecordServiceImpl implements InventoryRecordService {

    private final InventoryRecordMapper inventoryRecordMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<InventoryRecordVO> page(Long productId, String bizType, Integer pageNum, Integer pageSize) {
        Page<InventoryRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();

        if (productId != null) {
            wrapper.eq(InventoryRecord::getProductId, productId);
        }
        if (StrUtil.isNotBlank(bizType)) {
            wrapper.eq(InventoryRecord::getBizType, bizType);
        }
        wrapper.orderByDesc(InventoryRecord::getCreateTime);

        IPage<InventoryRecord> recordPage = inventoryRecordMapper.selectPage(page, wrapper);

        List<InventoryRecordVO> voList = recordPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        fillProductName(voList);
        fillOperatorName(voList);

        IPage<InventoryRecordVO> result = new Page<>(recordPage.getCurrent(), recordPage.getSize(),
                recordPage.getTotal());
        result.setRecords(voList);
        return result;
    }

    private InventoryRecordVO toVO(InventoryRecord record) {
        InventoryRecordVO vo = new InventoryRecordVO();
        vo.setId(record.getId());
        vo.setProductId(record.getProductId());
        vo.setType(record.getType());
        vo.setBizType(record.getBizType());
        vo.setQuantity(record.getQuantity());
        vo.setBeforeQuantity(record.getBeforeQuantity());
        vo.setAfterQuantity(record.getAfterQuantity());
        vo.setRefOrderNo(record.getRefOrderNo());
        vo.setRemark(record.getRemark());
        vo.setOperatorId(record.getOperatorId());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private void fillProductName(List<InventoryRecordVO> voList) {
        if (voList.isEmpty()) {
            return;
        }
        List<Long> productIds = voList.stream()
                .map(InventoryRecordVO::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return;
        }

        String placeholders = productIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, product_name FROM product WHERE id IN (" + placeholders + ") AND deleted = 0";

        List<Map<String, Object>> productList = jdbcTemplate.queryForList(sql, productIds.toArray());

        Map<Long, String> productNameMap = productList.stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("id")).longValue(),
                        m -> (String) m.get("product_name")));

        for (InventoryRecordVO vo : voList) {
            vo.setProductName(productNameMap.get(vo.getProductId()));
        }
    }

    private void fillOperatorName(List<InventoryRecordVO> voList) {
        if (voList.isEmpty()) {
            return;
        }
        List<Long> operatorIds = voList.stream()
                .map(InventoryRecordVO::getOperatorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (operatorIds.isEmpty()) {
            return;
        }

        String placeholders = operatorIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, real_name as operator_name FROM user WHERE id IN (" + placeholders
                + ") AND deleted = 0";

        try {
            List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, operatorIds.toArray());

            Map<Long, String> operatorNameMap = userList.stream()
                    .collect(Collectors.toMap(
                            m -> ((Number) m.get("id")).longValue(),
                            m -> (String) m.get("operator_name")));

            for (InventoryRecordVO vo : voList) {
                if (vo.getOperatorId() != null) {
                    vo.setOperatorName(operatorNameMap.getOrDefault(vo.getOperatorId(), "用户" + vo.getOperatorId()));
                }
            }
        } catch (Exception e) {
            // user表可能不在同一个数据库，降级处理
            for (InventoryRecordVO vo : voList) {
                if (vo.getOperatorId() != null) {
                    vo.setOperatorName("用户" + vo.getOperatorId());
                }
            }
        }
    }
}
