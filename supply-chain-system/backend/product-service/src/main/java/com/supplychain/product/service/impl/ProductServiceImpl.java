package com.supplychain.product.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.product.dto.ProductDTO;
import com.supplychain.product.dto.ProductQueryDTO;
import com.supplychain.product.entity.Category;
import com.supplychain.product.entity.Product;
import com.supplychain.product.mapper.CategoryMapper;
import com.supplychain.product.mapper.ProductMapper;
import com.supplychain.product.service.ProductService;
import com.supplychain.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IPage<ProductVO> page(ProductQueryDTO queryDTO) {
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Product::getProductName, queryDTO.getKeyword())
                    .or().like(Product::getProductCode, queryDTO.getKeyword()));
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Product::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> productPage = productMapper.selectPage(page, wrapper);

        Set<Long> categoryIds = productPage.getRecords().stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getCategoryName, (a, b) -> a));
        }

        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        IPage<ProductVO> voPage = productPage.convert(p -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            vo.setCategoryName(finalCategoryNameMap.get(p.getCategoryId()));
            return vo;
        });
        return voPage;
    }

    @Override
    public ProductVO getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ProductDTO dto) {
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, dto.getProductCode()));
        if (count > 0) {
            throw new BusinessException("商品编码已存在");
        }
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(1);
        productMapper.insert(product);

        jdbcTemplate.update(
                "INSERT INTO inventory(product_id, quantity, safety_stock, warehouse, update_time) VALUES(?,0,10,'主仓库',NOW())",
                product.getId());
    }

    @Override
    public void update(Long id, ProductDTO dto) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "商品不存在");
        }
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, dto.getProductCode())
                .ne(Product::getId, id));
        if (count > 0) {
            throw new BusinessException("商品编码已存在");
        }
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setId(id);
        productMapper.updateById(product);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public void checkProductExists(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
    }

    @Override
    public List<Product> getProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return productMapper.selectBatchIds(ids);
    }
}
