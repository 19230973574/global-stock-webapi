package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.CatalogMapper;
import com.globalstock.webapi.model.document.DataProductDocument;
import com.globalstock.webapi.model.document.ProductBundleDocument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CatalogInfra {

    private final CatalogMapper catalogMapper;

    public CatalogInfra(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public List<DataProductDocument> findActiveProducts() {
        try {
            return catalogMapper.findActiveProducts();
        } catch (Exception exception) {
            throw new SystemException("查询产品目录失败", exception);
        }
    }

    public Optional<DataProductDocument> findProductById(String id) {
        try {
            return catalogMapper.findProductById(id);
        } catch (Exception exception) {
            throw new SystemException("查询产品失败", exception);
        }
    }

    public DataProductDocument saveProduct(DataProductDocument document) {
        try {
            return catalogMapper.saveProduct(document);
        } catch (Exception exception) {
            throw new SystemException("保存产品失败", exception);
        }
    }

    public long countProducts() {
        try {
            return catalogMapper.countProducts();
        } catch (Exception exception) {
            throw new SystemException("统计产品失败", exception);
        }
    }

    public List<ProductBundleDocument> findActiveBundles() {
        try {
            return catalogMapper.findActiveBundles();
        } catch (Exception exception) {
            throw new SystemException("查询套餐失败", exception);
        }
    }

    public Optional<ProductBundleDocument> findBundleById(String id) {
        try {
            return catalogMapper.findBundleById(id);
        } catch (Exception exception) {
            throw new SystemException("查询套餐失败", exception);
        }
    }

    public ProductBundleDocument saveBundle(ProductBundleDocument document) {
        try {
            return catalogMapper.saveBundle(document);
        } catch (Exception exception) {
            throw new SystemException("保存套餐失败", exception);
        }
    }

    public long countBundles() {
        try {
            return catalogMapper.countBundles();
        } catch (Exception exception) {
            throw new SystemException("统计套餐失败", exception);
        }
    }
}
