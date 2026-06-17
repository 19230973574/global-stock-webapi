package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.mapper.CatalogMapper;
import com.globalstock.webapi.model.document.DataProductDocument;
import com.globalstock.webapi.model.document.ProductBundleDocument;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CatalogDomainService {

    private final CatalogMapper catalogMapper;

    public CatalogDomainService(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public List<DataProductDocument> listActiveProducts() {
        return catalogMapper.findActiveProducts();
    }

    public List<ProductBundleDocument> listActiveBundles() {
        return catalogMapper.findActiveBundles();
    }

    public Optional<DataProductDocument> findProduct(String productId) {
        return catalogMapper.findProductById(productId);
    }

    public DataProductDocument requireProduct(String productId) {
        return findProduct(productId)
                .filter(p -> "active".equals(p.getStatus()))
                .orElseThrow(() -> new BusinessException("数据产品不存在或已下架"));
    }

    public boolean isSupportedProduct(String productId) {
        return findProduct(productId)
                .filter(p -> "active".equals(p.getStatus()))
                .isPresent();
    }

    public void normalizeProductId(String productId) {
        if (!isSupportedProduct(productId)) {
            throw new BusinessException("不支持的数据权限");
        }
    }

    public static final String BUNDLE_PREFIX = "bundle:";

    public boolean isBundlePermission(String permission) {
        return permission != null && permission.startsWith(BUNDLE_PREFIX);
    }

    public String extractBundleId(String permission) {
        if (!isBundlePermission(permission)) {
            throw new BusinessException("无效的套餐标识");
        }
        return permission.substring(BUNDLE_PREFIX.length()).trim().toLowerCase(Locale.ROOT);
    }

    public Optional<ProductBundleDocument> findBundle(String bundleId) {
        return catalogMapper.findBundleById(bundleId);
    }

    public ProductBundleDocument requireBundle(String bundleId) {
        return findBundle(bundleId)
                .filter(b -> "active".equals(b.getStatus()))
                .orElseThrow(() -> new BusinessException("套餐不存在或已下架"));
    }

    public void normalizeOrderPermission(String permission) {
        if (isBundlePermission(permission)) {
            requireBundle(extractBundleId(permission));
            return;
        }
        normalizeProductId(permission);
    }

    public String resolveRequiredPermission(String market, String capability) {
        String normalizedMarket = market.toUpperCase(Locale.ROOT);
        String normalizedCapability = capability.toLowerCase(Locale.ROOT);
        return listActiveProducts().stream()
                .filter(p -> normalizedMarket.equalsIgnoreCase(p.getMarket())
                        && normalizedCapability.equals(p.getCapability()))
                .map(DataProductDocument::getId)
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的数据能力"));
    }

    public Optional<String> resolveMarketOpenPermission(String market) {
        return listActiveProducts().stream()
                .filter(p -> market.equalsIgnoreCase(p.getMarket())
                        && "market_open".equals(p.getCapability()))
                .map(DataProductDocument::getId)
                .findFirst();
    }
}
