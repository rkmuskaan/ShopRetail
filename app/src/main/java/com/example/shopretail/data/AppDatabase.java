package com.example.shopretail.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.shopretail.data.dao.ProductDao;
import com.example.shopretail.data.dao.SaleDao;
import com.example.shopretail.data.dao.StockOrderDao;
import com.example.shopretail.data.entity.Product;
import com.example.shopretail.data.entity.Sale;
import com.example.shopretail.data.entity.StockOrder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class, Sale.class, StockOrder.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductDao productDao();
    public abstract SaleDao saleDao();
    public abstract StockOrderDao stockOrderDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "shop_retail_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
