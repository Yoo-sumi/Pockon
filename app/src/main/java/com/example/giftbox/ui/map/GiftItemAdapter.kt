package com.example.giftbox.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class GiftItemAdapter (
    var gifts: List<Gift>,
    var context: Context
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
        holder.endDt.text = item.endDt
        holder.dDay.text = "D${getDday(item.endDt)}"
        holder.photo.load(item.photo) {
            transformations(RoundedCornersTransformation())
        }
    }

    inner class GiftItemAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val brand: TextView = view.findViewById(R.id.tv_brand)
        val name: TextView = view.findViewById(R.id.tv_name)
        val endDt: TextView = view.findViewById(R.id.tv_end_dt)
        val dDay: TextView = view.findViewById(R.id.tv_d_day)
        val photo: ImageView = view.findViewById(R.id.iv_photo)
    }

    private fun getDday(endDate: String): String {
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val current = LocalDateTime.now().format(formatter)

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val startDate = dateFormat.parse(current)?.time
        val parseEndDate = dateFormat.parse(endDate)?.time
        if (parseEndDate != null && startDate != null) {
            val diff = (startDate - parseEndDate) / (24 * 60 * 60 * 1000)
            return if (diff.toInt() > 0) {
                "+$diff"
            } else if (diff.toInt() == 0) {
                "-$diff"
            } else {
                "$diff"
            }
        }
        return ""
    }
}