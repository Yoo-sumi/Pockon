package com.example.giftbox.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday

class GiftItemAdapter (
    var gifts: List<Gift>,
    var context: Context,
    val onClick: (Gift) -> Unit
) : RecyclerView.Adapter<GiftItemAdapter.GiftItemAdapterViewHolder>() {

    override fun getItemCount(): Int {
        return gifts.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftItemAdapterViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_gift, parent, false)
        return GiftItemAdapterViewHolder(v)
    }

    override fun onBindViewHolder(holder: GiftItemAdapterViewHolder, position: Int) {
        val item = gifts[position]
        holder.brand.text = item.brand
        holder.name.text = item.name
        holder.endDt.text = context.getString(R.string.format_end_date, formatString(item.endDt))
        val formattedDay = getDday(item.endDt)
        if (formattedDay.second) {
            holder.dDayEnd.visibility = View.VISIBLE
            holder.dDay.visibility = View.GONE
            holder.dDayEnd.text = getDday(item.endDt).first
        } else {
            holder.dDayEnd.visibility = View.GONE
            holder.dDay.visibility = View.VISIBLE
            holder.dDay.text = getDday(item.endDt).first
        }
        holder.dDay.text = getDday(item.endDt).first
        holder.photo.load(item.photo)
        holder.cardView.setOnClickListener {
            onClick(item) // 상세보기
        }
    }

    inner class GiftItemAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val brand: TextView = view.findViewById(R.id.tv_brand)
        val name: TextView = view.findViewById(R.id.tv_name)
        val endDt: TextView = view.findViewById(R.id.tv_end_dt)
        val dDay: TextView = view.findViewById(R.id.tv_d_day)
        val dDayEnd: TextView = view.findViewById(R.id.tv_d_day_end)
        val photo: ImageView = view.findViewById(R.id.iv_photo)
        val cardView: CardView = view.findViewById(R.id.card_view)
    }
}