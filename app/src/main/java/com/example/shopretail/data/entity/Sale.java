package com.example.shopretail.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales",
        foreignKeys = @ForeignKey(entity = Product.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = ForeignKey.CASCADE))
public class Sale {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int productId;
    private int quantitySold;
    private double totalAmount;
    private long saleTimestamp;

    public Sale(int productId, int quantitySold, double totalAmount, long saleTimestamp) {
        this.productId = productId;
        this.quantitySold = quantitySold;
        this.totalAmount = totalAmount;
        this.saleTimestamp = saleTimestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantitySold() { return quantitySold; }
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public long getSaleTimestamp() { return saleTimestamp; }
    public void setSaleTimestamp(long saleTimestamp) { this.saleTimestamp = saleTimestamp; }
}
