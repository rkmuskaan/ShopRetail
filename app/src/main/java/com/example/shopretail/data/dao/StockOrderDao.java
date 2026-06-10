package com.example.shopretail.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.shopretail.data.entity.StockOrder;

import java.util.List;

@Dao
public interface StockOrderDao {
    @Insert
    void insert(StockOrder stockOrder);

    @Query("SELECT * FROM stock_orders ORDER BY orderTimestamp DESC")
    LiveData<List<StockOrder>> getAllStockOrders();
}
