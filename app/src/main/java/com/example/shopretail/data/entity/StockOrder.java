package com.example.shopretail.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "stock_orders",
        foreignKeys = @ForeignKey(entity = Product.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = ForeignKey.CASCADE))
public class StockOrder {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int productId;
    private int quantityOrdered;
    private double cost;
    private long orderTimestamp;

    public StockOrder(int productId, int quantityOrdered, double cost, long orderTimestamp) {
        this.productId = productId;
        this.quantityOrdered = quantityOrdered;
        this.cost = cost;
        this.orderTimestamp = orderTimestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public long getOrderTimestamp() { return orderTimestamp; }
    public void setOrderTimestamp(long orderTimestamp) { this.orderTimestamp = orderTimestamp; }
}
