package com.example.shopretail.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopretail.analytics.AnalyticsEngine;
import com.example.shopretail.databinding.ItemPurchaseOrderBinding;

import java.util.Locale;

public class PurchaseOrderAdapter extends ListAdapter<AnalyticsEngine.PredictionResult, PurchaseOrderAdapter.POViewHolder> {

    public PurchaseOrderAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<AnalyticsEngine.PredictionResult> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<AnalyticsEngine.PredictionResult>() {
        @Override
        public boolean areItemsTheSame(@NonNull AnalyticsEngine.PredictionResult oldItem, @NonNull AnalyticsEngine.PredictionResult newItem) {
            return oldItem.product.getId() == newItem.product.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AnalyticsEngine.PredictionResult oldItem, @NonNull AnalyticsEngine.PredictionResult newItem) {
            return oldItem.daysRemaining == newItem.daysRemaining &&
                    oldItem.recommendedReorderQty == newItem.recommendedReorderQty;
        }
    };

    @NonNull
    @Override
    public POViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPurchaseOrderBinding binding = ItemPurchaseOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new POViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull POViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class POViewHolder extends RecyclerView.ViewHolder {
        private final ItemPurchaseOrderBinding binding;

        POViewHolder(ItemPurchaseOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AnalyticsEngine.PredictionResult result) {
            binding.textViewProductName.setText(result.product.getName());
            binding.textViewVelocity.setText(String.format(Locale.getDefault(), "Velocity: %.2f units/day", result.dailyVelocity));
            binding.textViewDaysLeft.setText(String.format(Locale.getDefault(), "Days left: %d", result.daysRemaining));
            binding.textViewReorderRecommendation.setText(String.format(Locale.getDefault(), "Recommended Reorder: %d units", result.recommendedReorderQty));

            if (result.isCritical) {
                binding.textViewStatus.setVisibility(View.VISIBLE);
                binding.textViewStatus.setText("CRITICAL");
                binding.textViewStatus.setBackgroundColor(Color.RED);
            } else if (result.daysRemaining <= 14) {
                binding.textViewStatus.setVisibility(View.VISIBLE);
                binding.textViewStatus.setText("WARNING");
                binding.textViewStatus.setBackgroundColor(Color.parseColor("#FFA500")); // Orange
            } else {
                binding.textViewStatus.setVisibility(View.GONE);
            }
        }
    }
}
