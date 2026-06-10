package com.example.shopretail.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopretail.data.entity.Product;
import com.example.shopretail.databinding.ItemProductBinding;

import java.util.Locale;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    public ProductAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getSku().equals(newItem.getSku()) &&
                    oldItem.getCurrentStock() == newItem.getCurrentStock() &&
                    oldItem.getRetailPrice() == newItem.getRetailPrice();
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.textViewProductName.setText(product.getName());
            binding.textViewSKU.setText(String.format("SKU: %s", product.getSku()));
            binding.textViewStock.setText(String.valueOf(product.getCurrentStock()));
            binding.textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getRetailPrice()));
            binding.textViewCategory.setText(product.getCategory());

            if (product.getCurrentStock() <= product.getMinRequiredStock()) {
                binding.textViewLowStockWarning.setVisibility(View.VISIBLE);
            } else {
                binding.textViewLowStockWarning.setVisibility(View.GONE);
            }
        }
    }
}
