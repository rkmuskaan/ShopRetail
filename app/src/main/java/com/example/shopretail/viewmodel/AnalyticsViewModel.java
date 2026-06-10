package com.example.shopretail.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shopretail.analytics.AnalyticsEngine;
import com.example.shopretail.data.dao.SaleDao;
import com.example.shopretail.data.entity.Product;
import com.example.shopretail.data.repository.RetailRepository;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsViewModel extends AndroidViewModel {
    private final RetailRepository repository;
    private final MediatorLiveData<List<AnalyticsEngine.PredictionResult>> predictions = new MediatorLiveData<>();
    private final MediatorLiveData<AnalyticsEngine.ForecastResult> forecast = new MediatorLiveData<>();

    public AnalyticsViewModel(@NonNull Application application) {
        super(application);
        repository = new RetailRepository(application);

        LiveData<List<Product>> products = repository.getAllProducts();
        LiveData<List<SaleDao.DailyRevenue>> dailyRevenue = repository.getDailyRevenue();

        predictions.addSource(products, p -> updatePredictions(p));
        forecast.addSource(dailyRevenue, dr -> updateForecast(dr));
    }

    private void updatePredictions(List<Product> products) {
        if (products == null) return;
        
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        new Thread(() -> {
            List<SaleDao.ProductSalesVelocity> velocities = repository.getSalesVelocitySince(thirtyDaysAgo);
            List<AnalyticsEngine.PredictionResult> results = AnalyticsEngine.calculateInventoryPredictions(products, velocities, 30);
            predictions.postValue(results);
        }).start();
    }

    private void updateForecast(List<SaleDao.DailyRevenue> dailyRevenues) {
        if (dailyRevenues == null) return;
        forecast.setValue(AnalyticsEngine.forecastIncome(dailyRevenues));
    }

    public LiveData<List<AnalyticsEngine.PredictionResult>> getPredictions() {
        return predictions;
    }

    public LiveData<AnalyticsEngine.ForecastResult> getForecast() {
        return forecast;
    }
}
