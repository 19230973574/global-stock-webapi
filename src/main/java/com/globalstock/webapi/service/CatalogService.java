package com.globalstock.webapi.service;

import com.globalstock.webapi.model.document.DataProductDocument;
import com.globalstock.webapi.model.document.ProductBundleDocument;
import com.globalstock.webapi.model.dto.DataProductDTO;
import com.globalstock.webapi.model.dto.ProductBundleDTO;
import com.globalstock.webapi.service.domain.CatalogDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CatalogService {

    private final CatalogDomainService catalogDomainService;

    public CatalogService(CatalogDomainService catalogDomainService) {
        this.catalogDomainService = catalogDomainService;
    }

    public List<DataProductDTO> listProducts() {
        return catalogDomainService.listActiveProducts().stream()
                .map(this::toProductDTO)
                .toList();
    }

    public List<ProductBundleDTO> listBundles() {
        return catalogDomainService.listActiveBundles().stream()
                .map(this::toBundleDTO)
                .toList();
    }

    private DataProductDTO toProductDTO(DataProductDocument document) {
        return new DataProductDTO(
                document.getId(),
                document.getName(),
                document.getGroup(),
                document.getDescription(),
                document.getPriceAmount(),
                document.getPriceCurrency(),
                formatPrice(document.getPriceAmount(), document.getPriceCurrency()),
                document.getPeriodMonths(),
                document.getRequires() == null ? List.of() : List.copyOf(document.getRequires()),
                document.getApiQuotaMonthly(),
                document.getStatus()
        );
    }

    private ProductBundleDTO toBundleDTO(ProductBundleDocument document) {
        String display = document.getPriceLabel() != null && !document.getPriceLabel().isBlank()
                ? document.getPriceLabel()
                : formatPrice(document.getPriceAmount(), document.getPriceCurrency());
        return new ProductBundleDTO(
                document.getId(),
                document.getName(),
                document.getDescription(),
                document.getProductIds() == null ? List.of() : List.copyOf(document.getProductIds()),
                document.getPriceAmount(),
                document.getPriceCurrency(),
                display,
                document.getApiQuotaMonthly(),
                document.isPopular(),
                document.getStatus()
        );
    }

    private String formatPrice(Long amount, String currency) {
        if (amount == null) {
            return "定制";
        }
        String cur = currency == null ? "CNY" : currency.toUpperCase(Locale.ROOT);
        if ("CNY".equals(cur)) {
            return "¥" + (amount / 100) + "/月";
        }
        return cur + " " + (amount / 100) + "/月";
    }
}
