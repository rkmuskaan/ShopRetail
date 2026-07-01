package com.example.data.api

import android.util.Log
import com.example.data.local.CartItemEntity
import com.example.data.local.ProductEntity
import com.example.data.local.UserEntity
import com.example.data.local.CommentEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ChatMessageEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class FirestoreManager {

    val db: FirebaseFirestore = Firebase.firestore

    init {
        // Configure Firestore offline and online persistence to ensure product
        // data and cart items remain available disconnected or connected.
        val settings = firestoreSettings {
            // Enable offline persistence
            setLocalCacheSettings(persistentCacheSettings { })
        }
        db.firestoreSettings = settings
    }

    suspend fun syncUserOnline(user: UserEntity) {
        try {
            db.collection("users").document(user.idNumber)
                .set(user)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync user: ${e.message}")
        }
    }

    suspend fun getUserOnline(phone: String): UserEntity? {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("phoneNumber", phone)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                snapshot.documents.first().toObject(UserEntity::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to get user: ${e.message}")
            null
        }
    }

    suspend fun syncProductOnline(product: ProductEntity) {
        try {
            db.collection("products").document(product.id.toString())
                .set(product)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync product: ${e.message}")
        }
    }

    suspend fun removeProductOnline(productId: Int) {
        try {
            db.collection("products").document(productId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to delete product: ${e.message}")
        }
    }

    suspend fun syncCartItemOnline(cartItem: CartItemEntity) {
        try {
            // Document key combines user and product to make it unique per customer-product
            val docId = "${cartItem.customerId}_${cartItem.productId}"
            db.collection("carts").document(docId)
                .set(cartItem)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync cart item: ${e.message}")
        }
    }

    suspend fun removeCartItemOnline(customerId: String, productId: Int) {
        try {
            val docId = "${customerId}_${productId}"
            db.collection("carts").document(docId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to delete cart item: ${e.message}")
        }
    }

    suspend fun updateProductStockOnline(productId: Int, newStock: Int) {
        try {
            db.collection("products").document(productId.toString())
                .update("stockQuantity", newStock)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to update product stock: ${e.message}")
        }
    }

    suspend fun syncCommentOnline(comment: CommentEntity) {
        try {
            db.collection("comments").document(comment.commentId.toString())
                .set(comment)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync comment: ${e.message}")
        }
    }

    suspend fun syncTransactionOnline(tx: TransactionEntity) {
        try {
            db.collection("transactions").document(tx.txId.toString())
                .set(tx)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync transaction: ${e.message}")
        }
    }

    suspend fun syncOrderOnline(order: OrderEntity) {
        try {
            db.collection("orders").document(order.orderId.toString())
                .set(order)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync order: ${e.message}")
        }
    }

    suspend fun fetchProducts(): List<ProductEntity> {
        return try {
            val snapshot = db.collection("products").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ProductEntity::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun syncChatMessageOnline(message: ChatMessageEntity) {
        try {
            db.collection("chat_messages").document(message.messageId.toString())
                .set(message)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to sync chat message: ${e.message}")
        }
    }

    suspend fun setTypingStatus(senderId: String, receiverId: String, isTyping: Boolean) {
        try {
            val docRef = db.collection("typing_status").document("${senderId}_${receiverId}")
            if (isTyping) {
                docRef.set(mapOf("isTyping" to true, "timestamp" to System.currentTimeMillis())).await()
            } else {
                docRef.delete().await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to set typing status: ${e.message}")
        }
    }

    fun listenToTypingStatus(senderId: String, receiverId: String): Flow<Boolean> = callbackFlow {
        val listener = db.collection("typing_status").document("${senderId}_${receiverId}")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val isTyping = snapshot.getBoolean("isTyping") ?: false
                    val timestamp = snapshot.getLong("timestamp") ?: 0L
                    // Expire after 5 seconds
                    if (isTyping && System.currentTimeMillis() - timestamp < 5000) {
                        trySend(true)
                    } else {
                        trySend(false)
                    }
                } else {
                    trySend(false)
                }
            }
        awaitClose { listener.remove() }
    }

    fun listenToProducts(): Flow<List<ProductEntity>> = callbackFlow {
        val listener = db.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.toObjects(ProductEntity::class.java)
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }

    fun listenToCart(customerId: String): Flow<List<CartItemEntity>> = callbackFlow {
        val listener = db.collection("carts")
            .whereEqualTo("customerId", customerId)
            // Add metadata changes listener to get instant updates on sync status!
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val carts = snapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(CartItemEntity::class.java)
                    item?.copy(isSynced = !doc.metadata.hasPendingWrites())
                }
                trySend(carts)
            }
        }
        awaitClose { listener.remove() }
    }
}
