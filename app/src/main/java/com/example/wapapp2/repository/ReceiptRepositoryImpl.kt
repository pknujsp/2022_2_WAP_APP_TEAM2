package com.example.wapapp2.repository

import com.example.wapapp2.firebase.FireStoreNames
import com.example.wapapp2.model.ReceiptDTO
import com.example.wapapp2.model.ReceiptProductDTO
import com.example.wapapp2.repository.interfaces.ReceiptRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReceiptRepositoryImpl private constructor() : ReceiptRepository {
    private val fireStore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    companion object {
        private var INST: ReceiptRepositoryImpl? = null
        val INSTANCE get() = INST!!

        fun initialize() {
            INST = ReceiptRepositoryImpl()
        }

    }

    /** receipt_product 컬렉션의 참여 유저 id에서 자기 아이디 추가
     * when : checked
     * **/
    override suspend fun addMyID_fromProductParticipantIDs(product_id: String) {

    }

    /** receipt_product 컬렉션의 참여 유저 id에서 자기 아이디 제외
     * when : unchecked
     * **/
    override suspend fun subMyID_fromProductParticipantIDs(product_id: String) {

    }


    override suspend fun addReceipt(receiptDTO: ReceiptDTO, calcRoomId: String) = suspendCoroutine<Boolean> { continuation ->
        val receiptCollection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)
        receiptDTO.payersId = currentUser!!.uid

        receiptCollection.document().set(receiptDTO).addOnCompleteListener {
            continuation.resume(it.isSuccessful)
        }
    }

    override suspend fun addProducts(
            receiptId: String, productsList: MutableList<ReceiptProductDTO>, calcRoomId: String,
    ) = suspendCoroutine<Boolean> { continuation ->
        val writeBatch = fireStore.batch()

        for (receiptProduct in productsList) {
            writeBatch.set(fireStore.collection(FireStoreNames.calc_rooms.name)
                    .document(calcRoomId).collection(FireStoreNames.receipts.name)
                    .document(receiptId).collection(FireStoreNames.products.name).document(), receiptProduct)
        }

        writeBatch.commit().addOnCompleteListener {
            continuation.resume(it.isSuccessful)
        }
    }

    override suspend fun getLastDocumentId(calcRoomId: String) = suspendCoroutine<String?> { continuation ->
        val receiptCollection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)

        receiptCollection.orderBy("createdTime", Query.Direction.DESCENDING).limit(1)
                .get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        continuation.resume(it.result.documents[0].id)
                    } else {
                        continuation.resume(null)
                    }
                }
    }

    override suspend fun modifyReceipt(
            map: MutableMap<String, Any?>, calcRoomId: String,
            receiptId: String,
    ) = suspendCoroutine<Boolean> { continuation ->
        val receiptCollection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)
        receiptCollection.document(receiptId).update(map.toMap()).addOnCompleteListener {
            continuation.resume(it.isSuccessful)
        }
    }

    override suspend fun removeReceipt(calcRoomId: String, receiptId: String) = suspendCoroutine<Boolean> { continuation ->
        fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)
                .document(receiptId).delete().addOnCompleteListener {
                    continuation.resume(it.isSuccessful)
                }
    }


    override suspend fun removeProducts(
            calcRoomId: String, receiptId: String,
            removeIds: MutableList<String>,
    ) = suspendCoroutine<Boolean> { continuation ->
        val collection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)
                .document(receiptId).collection(FireStoreNames.products.name)

        fireStore.runBatch { batch ->
            for (removeId in removeIds) {
                batch.delete(collection.document(removeId))
            }
        }.addOnCompleteListener { continuation.resume(it.isSuccessful) }

    }

    override suspend fun modifyProducts(
            productMap: MutableMap<String, ReceiptProductDTO>, calcRoomId: String, receiptId: String,
    ) = suspendCoroutine<Boolean> { continuation ->
        val collection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)
                .document(receiptId).collection(FireStoreNames.products.name)

        fireStore.runBatch { batch ->
            for (product in productMap) {
                batch.set(collection.document(product.key), product.value)
            }
        }.addOnCompleteListener { continuation.resume(it.isSuccessful) }
    }


    override suspend fun getReceipts(calcRoomId: String) = suspendCoroutine<MutableList<ReceiptDTO>> { continuation ->
        val receiptCollection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name)
        receiptCollection.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val list = it.result.documents.toMutableList()
                var dto: ReceiptDTO? = null
                val dtoList = mutableListOf<ReceiptDTO>()

                for (v in list) {
                    dto = v.toObject<ReceiptDTO>()!!
                    dto.id = v.id
                    dtoList.add(dto)
                }
                continuation.resume(dtoList)
            } else {
                continuation.resume(mutableListOf())
            }
        }
    }

    override fun snapshotReceipts(calcRoomId: String, eventListener: EventListener<QuerySnapshot>): ListenerRegistration =
            fireStore.collection(FireStoreNames.calc_rooms.name)
                    .document(calcRoomId).collection(FireStoreNames.receipts.name).addSnapshotListener(eventListener)


    override suspend fun getProducts(
            receiptId: String,
            calcRoomId: String,
    ) = suspendCoroutine<MutableList<ReceiptProductDTO>> { continuation ->
        val productsCollection = fireStore.collection(FireStoreNames.calc_rooms.name)
                .document(calcRoomId).collection(FireStoreNames.receipts.name).document(receiptId).collection(FireStoreNames.products.name)
        productsCollection.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val list = it.result.documents.toMutableList()
                var dto: ReceiptProductDTO? = null
                val dtoList = mutableListOf<ReceiptProductDTO>()

                for (v in list) {
                    dto = v.toObject<ReceiptProductDTO>()!!
                    dto.id = v.id
                    dtoList.add(dto)
                }
                continuation.resume(dtoList)
            } else {
                continuation.resume(mutableListOf())
            }
        }
    }
}