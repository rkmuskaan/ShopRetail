package com.example.shopretail.analytics;

import com.example.shopretail.data.dao.SaleDao;
import com.example.shopretail.data.entity.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsEngine {

    public static class PredictionResult {
        public Product product;
        public double dailyVelocity;
        public int daysRemaining;
        public int recommendedReorderQty;
        public boolean isCritical;

        public PredictionResult(Product product, double dailyVelocity, int daysRemaining, int recommendedReorderQty) {
            this.product = product;
            this.dailyVelocity = dailyVelocity;
            this.daysRemaining = daysRemaining;
            this.recommendedReorderQty = recommendedReorderQty;
            this.isCritical = daysRemaining <= 7;
        }
    }

    public static List<PredictionResult> calculateInventoryPredictions(List<Product> products, List<SaleDao.ProductSalesVelocity> velocities, int periodDays) {
        List<PredictionResult> results = new ArrayList<>();
        Map<Integer, Integer> velocityMap = new HashMap<>();
        
        for (SaleDao.ProductSalesVelocity v : velocities) {
            velocityMap.put(v.productId, v.totalQty);
        }

        for (Product product : products) {
            int totalQtySold = velocityMap.getOrDefault(product.getId(), 0);
            double dailyVelocity = (double) totalQtySold / periodDays;
            
            int daysRemaining = dailyVelocity > 0 ? (int) (product.getCurrentStock() / dailyVelocity) : 999;
            
            // Recommended reorder to last for another 30 days plus safety stock
            int recommendedQty = 0;
            if (daysRemaining <= 10) {
                recommendedQty = (int) (dailyVelocity * 30) + product.getMinRequiredStock();
            }

            if (dailyVelocity > 0 || product.getCurrentStock() <= product.getMinRequiredStock()) {
                results.add(new PredictionResult(product, dailyVelocity, daysRemaining, recommendedQty));
            }
        }
        return results;
    }

    public static ForecastResult forecastIncome(List<SaleDao.DailyRevenue> historicalRevenue) {
        if (historicalRevenue.size() < 2) return new ForecastResult(0, 0);

        double total = 0;
        for (SaleDao.DailyRevenue dr : historicalRevenue) {
            total += dr.dailyTotal;
        }
        double avgDaily = total / historicalRevenue.size();

        return new ForecastResult(avgDaily * 7, avgDaily * 30);
    }

    public static class ForecastResult {
        public double nextWeekIncome;
        public double nextMonthIncome;

        public ForecastResult(double nextWeekIncome, double nextMonthIncome) {
            this.nextWeekIncome = nextWeekIncome;
            this.nextMonthIncome = nextMonthIncome;
        }
    }
}
