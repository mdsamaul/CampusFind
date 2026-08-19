package com.example.campusfind.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campusfind.ItemDetailsActivity
import com.example.campusfind.databinding.ItemCardBinding
import com.example.campusfind.models.Item

class ItemAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvItemTitle.text = item.title
            tvItemCategory.text = "Category: ${item.category}"
            tvItemLocation.text = "Location: ${item.location}"
            tvTimePosted.text = item.time
            chipType.text = item.type
            
            if (item.type == "Lost") {
                chipType.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
                chipType.setTextColor(Color.parseColor("#D32F2F"))
            } else {
                chipType.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
                chipType.setTextColor(Color.parseColor("#388E3C"))
            }

            root.setOnClickListener {
                val intent = Intent(root.context, ItemDetailsActivity::class.java)
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = items.size
}
