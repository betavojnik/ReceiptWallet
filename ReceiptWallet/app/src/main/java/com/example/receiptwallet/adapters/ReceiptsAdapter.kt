package com.example.receiptwallet.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.receiptwallet.R
import com.example.receiptwallet.databinding.ReceiptViewBinding
import com.example.receiptwallet.dto.ReceiptDto
import com.example.receiptwallet.models.Receipt
import com.example.receiptwallet.util.Constants
import com.example.receiptwallet.util.Constants.EXPIRED

class ReceiptsAdapter(private val context: Context) :
    RecyclerView.Adapter<ReceiptsAdapter.ReceiptsViewHolder>() {

    var onItemClick: ((ReceiptDto) -> Unit)? = null
    private var receiptList = ArrayList<ReceiptDto>()

    fun setReceipts(receiptDtos: ArrayList<ReceiptDto>) {
        this.receiptList = receiptDtos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptsViewHolder {
        return ReceiptsViewHolder(
            ReceiptViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return receiptList.size
    }

    override fun onBindViewHolder(holder: ReceiptsViewHolder, position: Int) {
        when (receiptList[position].category) {
            Constants.Category.HOUSE_APPLIANCE -> holder.binding.imageView.setImageResource(R.drawable.wasing_machine2)
            Constants.Category.IT_COMPONENT -> holder.binding.imageView.setImageResource(R.drawable.laptop)
            Constants.Category.VEHICLE -> holder.binding.imageView.setImageResource(R.drawable.vehicle)
            Constants.Category.TV_AUDIO_VIDEO -> holder.binding.imageView.setImageResource(R.drawable.tv)
            Constants.Category.FURNITURE -> holder.binding.imageView.setImageResource(R.drawable.furniture)
            else -> {
                holder.binding.imageView.setImageResource(R.drawable.other_second)
            }
        }
        holder.binding.tvName.text = receiptList[position].name
        holder.binding.tvTimeLeft.text = receiptList[position].timeLeft

        holder.binding.cvCard.setOnClickListener {
            onItemClick?.invoke(receiptList[position])
        }

        if (receiptList[position].timeLeft == EXPIRED) {
            val color = ContextCompat.getColor(context, R.color.test_home)
            holder.binding.tvTimeLeft.setTextColor(color)
        } else {
            val color = ContextCompat.getColor(context, R.color.black)
            holder.binding.tvTimeLeft.setTextColor(color)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class ReceiptsViewHolder(val binding: ReceiptViewBinding) :
        RecyclerView.ViewHolder(binding.root)
}