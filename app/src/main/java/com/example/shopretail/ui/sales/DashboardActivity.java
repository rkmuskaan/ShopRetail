package com.example.shopretail.ui.sales;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopretail.data.dao.ProductDao;
import com.example.shopretail.data.dao.SaleDao;
import com.example.shopretail.databinding.ActivityDashboardBinding;
import com.example.shopretail.viewmodel.SalesViewModel;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private SalesViewModel salesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        salesViewModel = new ViewModelProvider(this).get(SalesViewModel.class);

        observeMetrics();
        setupCharts();
    }

    private void observeMetrics() {
        salesViewModel.getTotalRevenue().observe(this, revenue -> {
            if (revenue != null) {
                binding.textViewTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f", revenue));
            }
        });

        salesViewModel.getTotalProfit().observe(this, profit -> {
            if (profit != null) {
                binding.textViewTotalProfit.setText(String.format(Locale.getDefault(), "$%.2f", profit));
            }
        });

        salesViewModel.getTotalItemsSold().observe(this, items -> {
            if (items != null) {
                binding.textViewTotalItems.setText(String.valueOf(items));
            }
        });
    }

    private void setupCharts() {
        // Line Chart: Revenue Trends
        salesViewModel.getDailyRevenue().observe(this, dailyRevenues -> {
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < dailyRevenues.size(); i++) {
                entries.add(new Entry(i, (float) dailyRevenues.get(i).dailyTotal));
            }
            LineDataSet dataSet = new LineDataSet(entries, "Daily Revenue");
            binding.lineChartRevenue.setData(new LineData(dataSet));
            binding.lineChartRevenue.invalidate();
        });

        // Bar Chart: Top Products
        salesViewModel.getTopSellingProducts().observe(this, topProducts -> {
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < topProducts.size(); i++) {
                entries.add(new BarEntry(i, topProducts.get(i).totalQty));
            }
            BarDataSet dataSet = new BarDataSet(entries, "Units Sold");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            binding.barChartTopProducts.setData(new BarData(dataSet));
            binding.barChartTopProducts.invalidate();
        });

        // Pie Chart: Stock by Category
        salesViewModel.getStockByCategory().observe(this, categoryStocks -> {
            List<PieEntry> entries = new ArrayList<>();
            for (ProductDao.CategoryStock cs : categoryStocks) {
                entries.add(new PieEntry(cs.totalStock, cs.category));
            }
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            binding.pieChartStock.setData(new PieData(dataSet));
            binding.pieChartStock.invalidate();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
