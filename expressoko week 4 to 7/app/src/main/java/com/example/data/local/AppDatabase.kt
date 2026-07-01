package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        CommentEntity::class,
        TransactionEntity::class,
        ChatMessageEntity::class,
        OrderEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ecommerceDao(): EcommerceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expressoko_secure_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // ensure seamless developers experience during phase iteration
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
