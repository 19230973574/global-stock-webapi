package com.globalstock.webapi.config;

import com.globalstock.webapi.mapper.CatalogMapper;
import com.globalstock.webapi.model.document.DataProductDocument;
import com.globalstock.webapi.model.document.ProductBundleDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时初始化产品目录与套餐（幂等）。
 */
@Component
@Order(10)
public class CatalogBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogBootstrap.class);

    private final CatalogMapper catalogMapper;

    public CatalogBootstrap(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (catalogMapper.countProducts() > 0) {
            return;
        }
        long now = System.currentTimeMillis();
        seedProducts(now);
        seedBundles(now);
        log.info("catalog bootstrap completed");
    }

    private void seedProducts(long now) {
        List<DataProductDocument> products = List.of(
                product("us_market_open", "美股开盘", "美股", "US", "market_open", "API 调用的基础准入权限", 99900L, 1, List.of(), null, 1, now),
                product("us_realtime", "美股实时", "美股", "US", "realtime", "Level 1 实时报价与成交量", 199900L, 1, List.of("us_market_open"), 10000L, 2, now),
                product("us_timeshare", "美股分时", "美股", "US", "timeshare", "分钟级分时走势数据", 299900L, 1, List.of("us_market_open"), 10000L, 3, now),
                product("us_history", "美股历史", "美股", "US", "history", "日 K 与历史成交数据", 399900L, 1, List.of("us_market_open"), 10000L, 4, now),
                product("a_realtime", "A股实时", "A股", "A", "realtime", "沪深实时行情快照", 149900L, 1, List.of(), 10000L, 5, now),
                product("etf_history", "ETF 历史", "ETF", "ETF", "history", "ETF 历史净值与成交", 99900L, 1, List.of(), 10000L, 6, now),
                product("us_quant_zone", "美股量化专区", "量化专区", "US", "quant", "新高新低、周期突破等美股量化筛选工具", 499900L, 1, List.of("us_history"), 500L, 7, now)
        );
        products.forEach(catalogMapper::saveProduct);
    }

    private void seedBundles(long now) {
        List<ProductBundleDocument> bundles = List.of(
                bundle("starter", "入门版", "适合个人开发者与小团队", List.of("us_market_open", "us_realtime", "etf_history"),
                        19900L, "¥199/月", 10000L, false, 1, now),
                bundle("pro", "专业版", "实时行情 + 多市场数据，适合量化团队",
                        List.of("us_market_open", "us_realtime", "us_timeshare", "a_realtime"),
                        99900L, "¥999/月", null, true, 2, now),
                bundle("enterprise", "机构版", "全市场覆盖 + 专属客户经理", List.of(
                        "us_market_open", "us_realtime", "us_timeshare", "us_history", "a_realtime", "etf_history"
                ), null, "定制", null, false, 3, now)
        );
        bundles.forEach(catalogMapper::saveBundle);
    }

    private DataProductDocument product(String id, String name, String group, String market, String capability,
                                        String desc, long price, int months, List<String> requires,
                                        Long quota, int sort, long now) {
        DataProductDocument doc = new DataProductDocument();
        doc.setId(id);
        doc.setName(name);
        doc.setGroup(group);
        doc.setMarket(market);
        doc.setCapability(capability);
        doc.setDescription(desc);
        doc.setPriceAmount(price);
        doc.setPriceCurrency("CNY");
        doc.setPeriodMonths(months);
        doc.setRequires(requires);
        doc.setApiQuotaMonthly(quota);
        doc.setStatus("active");
        doc.setSortOrder(sort);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        return doc;
    }

    private ProductBundleDocument bundle(String id, String name, String desc, List<String> productIds,
                                         Long price, String priceLabel, Long quota, boolean popular,
                                         int sort, long now) {
        ProductBundleDocument doc = new ProductBundleDocument();
        doc.setId(id);
        doc.setName(name);
        doc.setDescription(desc);
        doc.setProductIds(productIds);
        doc.setPriceAmount(price);
        doc.setPriceCurrency("CNY");
        doc.setPriceLabel(priceLabel);
        doc.setApiQuotaMonthly(quota);
        doc.setPopular(popular);
        doc.setStatus("active");
        doc.setSortOrder(sort);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        return doc;
    }
}
