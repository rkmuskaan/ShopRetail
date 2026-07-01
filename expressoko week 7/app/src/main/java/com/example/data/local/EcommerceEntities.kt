package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val idNumber: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val userType: String = "", // "Customer" or "Retailer"
    val passwordSet: String = "",
    val is2FAEnabled: Boolean = false,
    val profilePhotoPath: String = "",
    val businessPhotoPath: String = "",
    val interests: String = "",
    val businessName: String = "",
    val businessPhone: String = "",
    val businessNumber: String = "",
    val paymentType: String = "", // "Pochi la Biashara", "Paybill", "Lipa na MPESA till"
    val paymentNumber1: String = "", // Paybill number, Till number, or Phone number
    val paymentNumber2: String = "", // Account number for Paybill
    val paymentType2: String = "", 
    val paymentNumber3: String = "", 
    val paymentNumber4: String = "", 
    val appTheme: String = "Violet",
    val appLanguage: String = "en",
    val isSynced: Boolean = false
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "", // "Tech Gadget", "Food", "Snacks", "Spices", "Toiletries", etc.
    val description: String = "",
    val retailerId: String = "",
    val retailerPhone: String = "",
    val stockQuantity: Int = 0,
    val isPosted: Boolean = true, // true by default for old records
    val batchNumber: String = "",
    val expiryDate: String = "",
    val productImageUri: String = "",
    val paymentMode: String = "Cash/MPESA",
    val wholesalePieces: Int = 1,
    val maxCapacity: Int = 100,
    val itemIconEmoji: String = "📦", // Visual representational icon emoji
    val productWeight: String = "",
    val isPack: Boolean = false,
    val packConstituents: Int = 0,
    val additionalDescription: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int = 0,
    val quantity: Int = 0,
    val customerId: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "product_comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val commentId: Int = 0,
    val productId: Int = 0,
    val customerName: String = "",
    val commentText: String = "",
    val rating: Int = 5,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val txId: Int = 0,
    val customerId: String = "",
    val totalAmount: Double = 0.0,
    val checkoutRequestID: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "", // "SUCCESS", "PENDING", "FAILED"
    val itemSummary: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val customerId: String = "",
    val retailerId: String = "",
    val totalAmount: Double = 0.0,
    val itemSummary: String = "",
    val pickupTime: String = "",
    val status: String = "PENDING", // PENDING, PROCESSING, READY_FOR_PICKUP, DELIVERED, CANCELLED
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

// Relational structure joining cart item to corresponding product
data class CartProductJoin(
    @Embedded val cartItem: CartItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val messageId: Int = 0,
    val senderId: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val messageText: String = "",
    val attachmentUri: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val status: String = "Pending" // "Pending", "Sent", "Delivered", "Read", "Failed"
)

