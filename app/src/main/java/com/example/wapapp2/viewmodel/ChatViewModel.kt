package com.example.wapapp2.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.wapapp2.firebase.FireStoreNames
import com.example.wapapp2.model.CalcRoomDTO
import com.example.wapapp2.model.ChatDTO
import com.example.wapapp2.repository.ChatRepositorylmpl
import com.example.wapapp2.repository.interfaces.ChatRepository
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private lateinit var EnableChatRoom: CalcRoomDTO
    private val chatRepository: ChatRepository = ChatRepositorylmpl.getINSTANCE()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun attach(calcRoomDTO: CalcRoomDTO) {
        EnableChatRoom = calcRoomDTO
    }


    fun sendMsg(chatDTO: ChatDTO) {
        CoroutineScope(Dispatchers.Default).launch {
            chatRepository.sendMsg(EnableChatRoom.id!!, chatDTO)
        }
    }

    fun getOptions(calcRoomDTO: CalcRoomDTO): FirestoreRecyclerOptions<ChatDTO> {
        val query = Firebase.firestore
                .collection("calc_rooms")
                .document(calcRoomDTO.id!!)
                .collection("chats")
                .orderBy("sendedTime")

        val recyclerOption = FirestoreRecyclerOptions.Builder<ChatDTO>()
                .setQuery(query, SnapshotParser {
                    //id로부터 사람이름
                    ChatDTO(it.getString("userName").toString(), it.getTimestamp("sendedTime")?.toDate(), it.getString("msg").toString(), it.getString("senderId").toString())
                })
                .build()

        return recyclerOption
    }


    inner class ChatLiveData(val documentReference: DocumentReference) : LiveData<ChatDTO>(), EventListener<DocumentSnapshot> {
        private var snapshotListener: ListenerRegistration? = null

        override fun onActive() {
            super.onActive()
            snapshotListener = documentReference.addSnapshotListener(this)
        }

        override fun onInactive() {
            super.onInactive()
            snapshotListener?.remove()
        }


        override fun onEvent(result: DocumentSnapshot?, error: FirebaseFirestoreException?) {
            val item = result?.let { document ->
                document.toObject(ChatDTO::class.java)
            }
            value = item!!
        }
    }

    fun getQueryForOption(roomId: String): Query {
        val query = Firebase.firestore
                .collection(FireStoreNames.calc_rooms.name)
                .document(roomId)
                .collection(FireStoreNames.chats.name)
                .orderBy("sendedTime", Query.Direction.DESCENDING)

        /*
        query.addSnapshotListener(snapshotListener)
        //scroll 관련 구현 필요 snapshotlistener ??

         */
        return query
    }

    fun addSnapshot(roomId: String, listener: EventListener<QuerySnapshot>) {
        listenerRegistration = Firebase.firestore
                .collection(FireStoreNames.calc_rooms.name)
                .document(roomId)
                .collection(FireStoreNames.chats.name)
                .orderBy("sendedTime", Query.Direction.DESCENDING)
                .addSnapshotListener(listener)
    }
}