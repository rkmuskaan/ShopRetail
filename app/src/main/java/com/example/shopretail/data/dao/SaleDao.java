package com.example.shopretail.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shopretail.data.entity.Sale;

import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    void insert(Sale sale);

    @Query("SELECT * FROM sales ORDER BY saleTimestamp DESC")
    LiveData<List<Sale>> getAllSales();

    @Query("SELECT sales.*, products.name as productName FROM sales INNER JOIN products ON sales.productId = products.id ORDER BY saleTimestamp DESC")
    LiveData<List<SaleWithProduct>> getSalesWithProductNames();

    @Query("SELECT SUM(totalAmount) FROM sales")
    LiveData<Double> getTotalRevenue();

    @Query("SELECT SUM(sales.quantitySold * (products.retailPrice - products.purchasePrice)) FROM sales INNER JOIN products ON sales.productId = products.id")
    LiveData<Double> getTotalProfit();

    @Query("SELECT SUM(quantitySold) FROM sales")
    LiveData<Integer> getTotalItemsSold();

    @Query("SELECT productId, SUM(quantitySold) as totalQty FROM sales GROUP BY productId ORDER BY totalQty DESC LIMIT 5")
    LiveData<List<TopProduct>> getTopSellingProducts();

    @Query("SELECT date(saleTimestamp / 1000, 'unixepoch') as saleDate, SUM(totalAmount) as dailyTotal FROM sales GROUP BY saleDate ORDER BY saleDate ASC")
    LiveData<List<DailyRevenue>> getDailyRevenue();

    @Query("SELECT SUM(quantitySold) FROM sales WHERE saleTimestamp >= :start AND saleTimestamp <= :end")
    LiveData<Integer> getTodaySalesCount(long start, long end);

    @Query("SELECT AVG(totalAmount) FROM sales WHERE saleTimestamp >= :start AND saleTimestamp <= :end")
    LiveData<Double> getTodayAvgSalesValue(long start, long end);

    @Query("SELECT SUM(sales.quantitySold * (products.retailPrice - products.purchasePrice)) FROM sales INNER JOIN products ON sales.productId = products.id WHERE saleTimestamp >= :start AND saleTimestamp <= :end")
    LiveData<Double> getTodayProfit(long start, long end);

    @Query("SELECT SUM(sales.quantitySold * (products.retailPrice - products.purchasePrice)) FROM sales INNER JOIN products ON sales.productId = products.id WHERE saleTimestamp >= :start AND saleTimestamp <= :end")
    LiveData<Double> getPeriodProfit(long start, long end);

    @Query("SELECT productId, SUM(quantitySold) as totalQty FROM sales WHERE saleTimestamp >= :since GROUP BY productId")
    List<ProductSalesVelocity> getSalesVelocitySince(long since);

    class SaleWithProduct {
        public int id;
        public int productId;
        public int quantitySold;
        public double totalAmount;
        public long saleTimestamp;
        public String productName;
    }

    class TopProduct {
        public int productId;
        public int totalQty;
    }

    class DailyRevenue {
        public String saleDate;
        public double dailyTotal;
    }

    class ProductSalesVelocity {
        public int productId;
        public int totalQty;
    }
}
