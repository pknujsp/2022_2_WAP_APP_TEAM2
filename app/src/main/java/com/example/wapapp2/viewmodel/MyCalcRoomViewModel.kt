package com.example.wapapp2.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wapapp2.firebase.FireStoreNames
import com.example.wapapp2.model.CalcRoomDTO
import com.example.wapapp2.model.UserDTO
import com.example.wapapp2.repository.CalcRoomRepositorylmpl
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MyCalcRoomViewModel : ViewModel() {
    private val myCalcRoomRepositoryImpl = CalcRoomRepositorylmpl.getINSTANCE()
    private val fireStore = FirebaseFirestore.getInstance()
    val myCalcRoomIds = MutableLiveData<MutableSet<String>>(mutableSetOf())

    private var myCalcRoomIdsListener: ListenerRegistration? = null

    fun getMyCalcRoomsOptions(): FirestoreRecyclerOptions<CalcRoomDTO> {
        val query = fireStore.collection(FireStoreNames.calc_rooms.name)
                .whereIn(FieldPath.documentId(), myCalcRoomIds.value!!.toMutableList())

        val options = FirestoreRecyclerOptions.Builder<CalcRoomDTO>()
                .setQuery(query, MetadataChanges.INCLUDE) { snapshot ->
                    val dto = snapshot.toObject<CalcRoomDTO>()!!
                    dto.id = snapshot.id
                    dto
                }.build()
        return options

    }

    fun exitFromCalcRoom(roomId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            val result = async {
                myCalcRoomRepositoryImpl.exitFromCalcRoom(roomId)
            }
            result.await()
        }
    }

    override fun onCleared() {
        super.onCleared()
        myCalcRoomIdsListener?.remove()
    }


    fun loadMyCalcRoomIds() {
        if (myCalcRoomIdsListener == null) {
            myCalcRoomIdsListener = myCalcRoomRepositoryImpl.getMyCalcRoomIds { event, error ->
                if (event != null) {
                    val userDTO = event!!.toObject<UserDTO>()
                    userDTO?.also {
                        val newIdSet = it.myCalcRoomIds.toMutableSet()

                        //id가 수정된 게 있는 지 확인
                        if (newIdSet != myCalcRoomIds.value) {
                            myCalcRoomIds.value = newIdSet
                        }
                    }
                }

            }
        }
    }

}