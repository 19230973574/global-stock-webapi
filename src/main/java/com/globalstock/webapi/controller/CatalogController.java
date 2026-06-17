package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.DataProductDTO;
import com.globalstock.webapi.model.dto.ProductBundleDTO;
import com.globalstock.webapi.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/catalog/v1")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public ApiResponse<List<DataProductDTO>> listProducts() {
        return ApiResponse.success(catalogService.listProducts());
    }

    @GetMapping("/bundles")
    public ApiResponse<List<ProductBundleDTO>> listBundles() {
        return ApiResponse.success(catalogService.listBundles());
    }
}
