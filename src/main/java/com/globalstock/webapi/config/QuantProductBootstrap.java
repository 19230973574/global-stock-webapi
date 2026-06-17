package com.globalstock.webapi.config;

import com.globalstock.webapi.mapper.CatalogMapper;
import com.globalstock.webapi.model.document.DataProductDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 幂等补充量化专区产品目录。
 */
@Component
@Order(11)
public class QuantProductBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuantProductBootstrap.class);

    private final CatalogMapper catalogMapper;

    public QuantProductBootstrap(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (catalogMapper.findProductById("us_quant_zone").isPresent()) {
            return;
        }
        long now = System.currentTimeMillis();
        DataProductDocument product = new DataProductDocument();
        product.setId("us_quant_zone");
        product.setName("美股量化专区");
        product.setGroup("量化专区");
        product.setMarket("US");
        product.setCapability("quant");
        product.setDescription("新高新低、周期突破等美股量化筛选工具");
        product.setPriceAmount(499900L);
        product.setPriceCurrency("CNY");
        product.setPeriodMonths(1);
        product.setRequires(List.of("us_history"));
        product.setApiQuotaMonthly(500L);
        product.setStatus("active");
        product.setSortOrder(7);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        catalogMapper.saveProduct(product);
        log.info("quant product bootstrap completed id=us_quant_zone");
    }
}
