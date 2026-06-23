package com.lethanh.ql_com_dao_bk.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Category {
    private Integer id;
    private String label;
    private String description;
    private String badge;

    @SerializedName("products")
    private List<Product> products;

    @SerializedName("product_ids")
    private List<Integer> productIds;

    public Category() {
    }

    public Category(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public Category(String label, String description, List<Integer> productIds) {
        this.label = label;
        this.description = description;
        this.productIds = productIds;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setId(String id) {
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            this.id = null;
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Integer> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Integer> productIds) {
        this.productIds = productIds;
    }
}
