package com.supplychain.product.service;

import com.supplychain.product.dto.CategoryDTO;
import com.supplychain.product.entity.Category;
import com.supplychain.product.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> list();

    void create(CategoryDTO dto);

    void update(Long id, CategoryDTO dto);

    void delete(Long id);

    Category getById(Long id);
}
