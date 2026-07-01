package com.example.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.api.FirestoreManager
import com.example.data.local.AppDatabase

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting sync process...")
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.ecommerceDao()
        val firestoreManager = FirestoreManager()

        try {
            // 1. Fetch Remote Updates First (Down sync)
            val remoteProducts = firestoreManager.fetchProducts()
            for (remoteProduct in remoteProducts) {
                // Assuming we just upsert
                dao.insertProduct(remoteProduct.copy(isSynced = true))
            }

            // 2. Sync Users (Up sync)
            val unsyncedUsers = dao.getUnsyncedUsers()
            for (user in unsyncedUsers) {
                firestoreManager.syncUserOnline(user)
                dao.insertUser(user.copy(isSynced = true))
            }

            // 2. Sync Products
            val unsyncedProducts = dao.getUnsyncedProducts()
            for (product in unsyncedProducts) {
                firestoreManager.syncProductOnline(product)
                dao.insertProduct(product.copy(isSynced = true))
            }

            // 3. Sync Cart Items
            val unsyncedCartItems = dao.getUnsyncedCartItems()
            for (cartItem in unsyncedCartItems) {
                firestoreManager.syncCartItemOnline(cartItem)
                dao.addOrUpdateCartItem(cartItem.copy(isSynced = true))
            }

            // 4. Sync Comments
            val unsyncedComments = dao.getUnsyncedComments()
            for (comment in unsyncedComments) {
                firestoreManager.syncCommentOnline(comment)
                dao.insertComment(comment.copy(isSynced = true))
            }

            // 5. Sync Transactions
            val unsyncedTransactions = dao.getUnsyncedTransactions()
            for (tx in unsyncedTransactions) {
                firestoreManager.syncTransactionOnline(tx)
                dao.insertTransaction(tx.copy(isSynced = true))
            }

            // 6. Sync Orders
            val unsyncedOrders = dao.getUnsyncedOrders()
            for (order in unsyncedOrders) {
                firestoreManager.syncOrderOnline(order)
                dao.insertOrder(order.copy(isSynced = true))
            }

            // 7. Sync Chat Messages
            val unsyncedChatMessages = dao.getUnsyncedChatMessages()
            for (msg in unsyncedChatMessages) {
                firestoreManager.syncChatMessageOnline(msg)
                dao.insertChatMessage(msg.copy(isSynced = true))
            }

            Log.d("SyncWorker", "Sync process completed successfully.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync: ${e.message}")
            return Result.retry()
        }
    }
}
