package com.supplychain.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.product.dto.SupplierDTO;
import com.supplychain.product.entity.Supplier;
import com.supplychain.product.vo.SupplierVO;

import java.util.List;

public interface SupplierService {

    IPage<SupplierVO> page(String keyword, Integer pageNum, Integer pageSize);

    List<SupplierVO> listAll();

    Supplier getById(Long id);

    void create(SupplierDTO dto);

    void update(Long id, SupplierDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);
}
