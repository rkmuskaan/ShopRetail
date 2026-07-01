package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EcommerceDao {

    // --- Users Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE idNumber = :idNo LIMIT 1")
    suspend fun getUserById(idNo: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<UserEntity>


    // --- Products Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE retailerId = :retailerId ORDER BY id DESC")
    fun getProductsByRetailer(retailerId: String): Flow<List<ProductEntity>>

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, newStock: Int)


    // --- Cart Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateCartItem(cartItem: CartItemEntity)

    @Transaction
    @Query("SELECT * FROM cart_items WHERE customerId = :customerId ORDER BY addedAt DESC")
    fun getCartWithProducts(customerId: String): Flow<List<CartProductJoin>>

    @Transaction
    @Query("SELECT * FROM cart_items WHERE productId IN (SELECT id FROM products WHERE retailerId = :retailerId) ORDER BY addedAt DESC")
    fun getPendingCartItemsForRetailer(retailerId: String): Flow<List<CartProductJoin>>

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId AND customerId = :customerId")
    suspend fun updateCartQuantity(productId: Int, quantity: Int, customerId: String)

    @Query("DELETE FROM cart_items WHERE productId = :productId AND customerId = :customerId")
    suspend fun removeProductFromCart(productId: Int, customerId: String)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCart(customerId: String)


    // --- Comments & Feedback ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM product_comments WHERE productId = :productId ORDER BY timestamp DESC")
    fun getCommentsForProduct(productId: Int): Flow<List<CommentEntity>>


    // --- Transactions & Sales Reporting ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity): Long

    @Query("UPDATE transactions SET status = :status WHERE txId = :txId")
    suspend fun updateTransactionStatus(txId: Int, status: String)

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomer(customerId: String): Flow<List<TransactionEntity>>

    // General transaction metrics for reporting (all transactions since we are locally simulated)
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // --- Chat Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getChatMessagesBetweenUsers(user1: String, user2: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getAllChatMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>
    
    @Query("DELETE FROM chat_messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1)")
    suspend fun deleteChatBetweenUsers(user1: String, user2: String)

    // --- Order Status System ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getCustomerOrders(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE retailerId = :retailerId ORDER BY timestamp DESC")
    fun getRetailerOrders(retailerId: String): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET status = :newStatus WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: Int, newStatus: String)

    // --- Sync Queries ---
    @Query("SELECT * FROM users WHERE isSynced = 0")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("SELECT * FROM cart_items WHERE isSynced = 0")
    suspend fun getUnsyncedCartItems(): List<CartItemEntity>

    @Query("SELECT * FROM product_comments WHERE isSynced = 0")
    suspend fun getUnsyncedComments(): List<CommentEntity>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM orders WHERE isSynced = 0")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("SELECT * FROM chat_messages WHERE isSynced = 0")
    suspend fun getUnsyncedChatMessages(): List<ChatMessageEntity>
}
