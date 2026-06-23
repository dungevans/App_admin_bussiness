package com.lethanh.ql_com_dao_bk.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    private Integer id;
    private String label;
    private String description;
    private double price;
    private String currency;
    private String unit;
    private String badge;
    private boolean retrievable;
    private String created;
    
    @SerializedName("image_url")
    private String imageUrl;

    public Product() {}

    public Product(String label, String description, double price, String currency, String unit, String badge, String imageUrl) {
        this.label = label;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.unit = unit;
        this.badge = badge;
        this.imageUrl = imageUrl;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public void setId(String id) {
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            this.id = null;
        }
    }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isRetrievable() { return retrievable; }
    public void setRetrievable(boolean retrievable) { this.retrievable = retrievable; }
    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }
}
