package com.example.wapapp2.view.calendar.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wapapp2.R
import com.example.wapapp2.databinding.DialogCalculationItemBinding
import com.example.wapapp2.model.ReceiptDTO
import com.example.wapapp2.view.calculation.CalcMainFragment
import com.example.wapapp2.view.main.MainHostFragment
import org.joda.time.DateTime

class ReceiptListForADayAdapter(private val list: ArrayList<ReceiptDTO>, val receiptItemClickListener: ReceiptItemClickListener) : RecyclerView.Adapter<ReceiptListForADayAdapter.ViewHolder>() {


    inner class ViewHolder(private val binding: DialogCalculationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val dstReceipt = list[position]
            binding.receiptName.text = dstReceipt.name
            //binding.payer.text = dstReceipt.payersId.
            binding.time.text = DateTime.parse(dstReceipt.date.toString()).toString("aa hh:mm")
            binding.totalMoney.text = dstReceipt.totalMoney.toString()
            binding.status.text = if(dstReceipt.status) "정산 완료" else "정산 진행중.."

            binding.root.setOnClickListener(View.OnClickListener { receiptItemClickListener.OnReceiptClicked(dstReceipt.roomID) })

            // payer -> 이름으로 구현 등등 구현 필요
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DialogCalculationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = list.size
}
