package com.example.wapapp2.view.calculation.calcroom.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wapapp2.commons.interfaces.IAdapterItemCount
import com.example.wapapp2.commons.interfaces.ListOnClickListener
import com.example.wapapp2.databinding.GroupItemBinding
import com.example.wapapp2.model.CalcRoomDTO
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class GroupAdapter(
        options: FirestoreRecyclerOptions<CalcRoomDTO>,
        private val onItemClickListener: ListOnClickListener<CalcRoomDTO>,
) :
        FirestoreRecyclerAdapter<CalcRoomDTO, GroupAdapter.GroupVH>(options), IAdapterItemCount {

    class GroupVH(
            private val binding: GroupItemBinding,
            private val onItemClickListener: ListOnClickListener<CalcRoomDTO>,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd E", Locale.getDefault())

        fun bind(calcRoomDTO: CalcRoomDTO) {
            /*
            var nameString = ""
            var count = 1;
            for (name in calcRoomDTO.participantIds) {
                if (count > 3) {
                    nameString += " 외 " + (calcRoomDTO.members.size - 3) + "명"
                    break
                } else {
                    nameString += name
                    if (count != calcRoomDTO.members.size && count != 3)
                        nameString += ", "
                }
                count++
            }

             */

            binding.groupItemNames.text = calcRoomDTO.name
            //binding.groupItemState.text = calcRoomDTO.state
            binding.groupItemDate.text = dateFormat.format(calcRoomDTO.createdTime!!)

            binding.root.setOnClickListener {
                onItemClickListener.onClicked(calcRoomDTO, bindingAdapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            GroupVH(GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), onItemClickListener)


    override fun onBindViewHolder(holder: GroupVH, position: Int, model: CalcRoomDTO) {
        holder.bind(model)
    }

    override fun getAdapterItemCount(): Int {
        return itemCount
    }


}