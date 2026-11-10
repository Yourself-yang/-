package com.supplychain.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.product.dto.SupplierDTO;
import com.supplychain.product.entity.Supplier;
import com.supplychain.product.mapper.SupplierMapper;
import com.supplychain.product.service.SupplierService;
import com.supplychain.product.vo.SupplierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    @Override
    public IPage<SupplierVO> page(String keyword, Integer pageNum, Integer pageSize) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Supplier::getSupplierName, keyword)
                    .or().like(Supplier::getContactPerson, keyword)
                    .or().like(Supplier::getContactPhone, keyword));
        }
        wrapper.orderByDesc(Supplier::getCreateTime);
        IPage<Supplier> supplierPage = supplierMapper.selectPage(page, wrapper);
        return supplierPage.convert(s -> {
            SupplierVO vo = new SupplierVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        });
    }

    @Override
    public List<SupplierVO> listAll() {
        List<Supplier> suppliers = supplierMapper.selectList(
                new LambdaQueryWrapper<Supplier>().orderByDesc(Supplier::getCreateTime));
        return suppliers.stream().map(s -> {
            SupplierVO vo = new SupplierVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public void create(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        if (supplier.getLevel() == null) {
            supplier.setLevel(3);
        }
        if (supplier.getStatus() == null) {
            supplier.setStatus(1);
        }
        supplierMapper.insert(supplier);
    }

    @Override
    public void update(Long id, SupplierDTO dto) {
        Supplier existing = supplierMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "供应商不存在");
        }
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        supplier.setId(id);
        supplierMapper.updateById(supplier);
    }

    @Override
    public void delete(Long id) {
        supplierMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setStatus(status);
        supplierMapper.updateById(supplier);
    }
}
