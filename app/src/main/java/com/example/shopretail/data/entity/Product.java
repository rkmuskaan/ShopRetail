package com.example.shopretail.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String sku;
    private int currentStock;
    private int minRequiredStock;
    private double purchasePrice;
    private double retailPrice;
    private String category;

    public Product(String name, String sku, int currentStock, int minRequiredStock, double purchasePrice, double retailPrice, String category) {
        this.name = name;
        this.sku = sku;
        this.currentStock = currentStock;
        this.minRequiredStock = minRequiredStock;
        this.purchasePrice = purchasePrice;
        this.retailPrice = retailPrice;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public int getMinRequiredStock() { return minRequiredStock; }
    public void setMinRequiredStock(int minRequiredStock) { this.minRequiredStock = minRequiredStock; }
    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public double getRetailPrice() { return retailPrice; }
    public void setRetailPrice(double retailPrice) { this.retailPrice = retailPrice; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
