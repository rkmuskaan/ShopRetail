package com.example.shopretail.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.shopretail.data.AppDatabase;
import com.example.shopretail.data.dao.ProductDao;
import com.example.shopretail.data.dao.SaleDao;
import com.example.shopretail.data.dao.StockOrderDao;
import com.example.shopretail.data.entity.Product;
import com.example.shopretail.data.entity.Sale;
import com.example.shopretail.data.entity.StockOrder;

import java.util.List;

public class RetailRepository {
    private final ProductDao productDao;
    private final SaleDao saleDao;
    private final StockOrderDao stockOrderDao;
    private final LiveData<List<Product>> allProducts;

    public RetailRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
        saleDao = db.saleDao();
        stockOrderDao = db.stockOrderDao();
        allProducts = productDao.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> getLowStockProducts() {
        return productDao.getLowStockProducts();
    }

    public void insertProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.insert(product));
    }

    public void updateProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.update(product));
    }

    public void insertSale(Sale sale) {
        AppDatabase.databaseWriteExecutor.execute(() -> saleDao.insert(sale));
    }

    public LiveData<List<Sale>> getAllSales() {
        return saleDao.getAllSales();
    }

    public LiveData<List<SaleDao.SaleWithProduct>> getSalesWithProducts() {
        return saleDao.getSalesWithProductNames();
    }

    public void insertStockOrder(StockOrder stockOrder) {
        AppDatabase.databaseWriteExecutor.execute(() -> stockOrderDao.insert(stockOrder));
    }

    public LiveData<Double> getTotalRevenue() {
        return saleDao.getTotalRevenue();
    }

    public LiveData<Double> getTotalProfit() {
        return saleDao.getTotalProfit();
    }

    public LiveData<Integer> getTotalItemsSold() {
        return saleDao.getTotalItemsSold();
    }

    public LiveData<List<SaleDao.TopProduct>> getTopSellingProducts() {
        return saleDao.getTopSellingProducts();
    }

    public LiveData<List<SaleDao.DailyRevenue>> getDailyRevenue() {
        return saleDao.getDailyRevenue();
    }

    public LiveData<Integer> getTodaySalesCount(long start, long end) {
        return saleDao.getTodaySalesCount(start, end);
    }

    public LiveData<Double> getTodayAvgSalesValue(long start, long end) {
        return saleDao.getTodayAvgSalesValue(start, end);
    }

    public LiveData<Double> getTodayProfit(long start, long end) {
        return saleDao.getTodayProfit(start, end);
    }

    public LiveData<Double> getPeriodProfit(long start, long end) {
        return saleDao.getPeriodProfit(start, end);
    }

    public LiveData<List<ProductDao.CategoryStock>> getStockByCategory() {
        return productDao.getStockByCategory();
    }

    public List<SaleDao.ProductSalesVelocity> getSalesVelocitySince(long since) {
        return saleDao.getSalesVelocitySince(since);
    }
}
