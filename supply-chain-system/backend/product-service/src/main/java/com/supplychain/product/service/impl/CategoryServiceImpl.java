package com.supplychain.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.product.dto.CategoryDTO;
import com.supplychain.product.entity.Category;
import com.supplychain.product.mapper.CategoryMapper;
import com.supplychain.product.service.CategoryService;
import com.supplychain.product.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> list() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort).orderByDesc(Category::getCreateTime));
        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void create(CategoryDTO dto) {
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Category parent = categoryMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
        }
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        categoryMapper.insert(category);
    }

    @Override
    public void update(Long id, CategoryDTO dto) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setId(id);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }
}
