package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.DataProductDocument;
import com.globalstock.webapi.model.document.ProductBundleDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CatalogMapper {

    private final MongoTemplate mongoTemplate;

    public CatalogMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public DataProductDocument saveProduct(DataProductDocument document) {
        return mongoTemplate.save(document);
    }

    public ProductBundleDocument saveBundle(ProductBundleDocument document) {
        return mongoTemplate.save(document);
    }

    public long countProducts() {
        return mongoTemplate.count(new Query(), DataProductDocument.class);
    }

    public List<DataProductDocument> findActiveProducts() {
        Query query = Query.query(Criteria.where("status").is("active"))
                .with(Sort.by(Sort.Direction.ASC, "sort_order"));
        return mongoTemplate.find(query, DataProductDocument.class);
    }

    public Optional<DataProductDocument> findProductById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, DataProductDocument.class));
    }

    public List<ProductBundleDocument> findActiveBundles() {
        Query query = Query.query(Criteria.where("status").is("active"))
                .with(Sort.by(Sort.Direction.ASC, "sort_order"));
        return mongoTemplate.find(query, ProductBundleDocument.class);
    }

    public Optional<ProductBundleDocument> findBundleById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, ProductBundleDocument.class));
    }

    public long countBundles() {
        return mongoTemplate.count(new Query(), ProductBundleDocument.class);
    }
}
