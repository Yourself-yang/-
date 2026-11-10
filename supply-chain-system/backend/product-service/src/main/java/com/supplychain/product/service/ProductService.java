package com.supplychain.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.supplychain.product.dto.ProductDTO;
import com.supplychain.product.dto.ProductQueryDTO;
import com.supplychain.product.entity.Product;
import com.supplychain.product.vo.ProductVO;

import java.util.List;

public interface ProductService {

    IPage<ProductVO> page(ProductQueryDTO queryDTO);

    ProductVO getById(Long id);

    void create(ProductDTO dto);

    void update(Long id, ProductDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    void checkProductExists(Long id);

    List<Product> getProductsByIds(List<Long> ids);
}
