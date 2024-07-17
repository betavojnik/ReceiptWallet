package com.example.receiptwallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(private var listOfCategories: ArrayList<String>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfCategories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listOfCategories[position]
        holder.item.text = item
        holder.itemView.setOnClickListener {
            listener.onItemClick(item)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item: TextView = itemView.findViewById(R.id.tvCategoryItem)
    }

    interface OnItemClickListener {
        fun onItemClick(item: String)
    }
}