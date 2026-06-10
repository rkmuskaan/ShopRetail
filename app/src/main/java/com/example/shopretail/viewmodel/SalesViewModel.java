package com.example.shopretail.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.shopretail.data.dao.ProductDao;
import com.example.shopretail.data.dao.SaleDao;
import com.example.shopretail.data.entity.Sale;
import com.example.shopretail.data.repository.RetailRepository;

import java.util.Calendar;
import java.util.List;

public class SalesViewModel extends AndroidViewModel {
    private final RetailRepository repository;

    public SalesViewModel(@NonNull Application application) {
        super(application);
        repository = new RetailRepository(application);
    }

    public LiveData<Integer> getTodaySalesCount() {
        long[] range = getTodayRange();
        return repository.getTodaySalesCount(range[0], range[1]);
    }

    public LiveData<Double> getTodayAvgSalesValue() {
        long[] range = getTodayRange();
        return repository.getTodayAvgSalesValue(range[0], range[1]);
    }

    public LiveData<Double> getTodayProfit() {
        long[] range = getTodayRange();
        return repository.getTodayProfit(range[0], range[1]);
    }

    public LiveData<Double> getYesterdayProfit() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long start = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long end = cal.getTimeInMillis();
        return repository.getPeriodProfit(start, end);
    }

    private long[] getTodayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long start = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long end = cal.getTimeInMillis();
        return new long[]{start, end};
    }

    public void insertSale(Sale sale) {
        repository.insertSale(sale);
    }

    public LiveData<Double> getTotalRevenue() {
        return repository.getTotalRevenue();
    }

    public LiveData<Double> getTotalProfit() {
        return repository.getTotalProfit();
    }

    public LiveData<Integer> getTotalItemsSold() {
        return repository.getTotalItemsSold();
    }

    public LiveData<List<SaleDao.TopProduct>> getTopSellingProducts() {
        return repository.getTopSellingProducts();
    }

    public LiveData<List<SaleDao.DailyRevenue>> getDailyRevenue() {
        return repository.getDailyRevenue();
    }

    public LiveData<List<ProductDao.CategoryStock>> getStockByCategory() {
        return repository.getStockByCategory();
    }
}
