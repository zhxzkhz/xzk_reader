package com.zhhz.reader.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.zhhz.reader.R
import com.zhhz.reader.bean.DrawerItem
import com.zhhz.reader.bean.FlexChildStyle

class DrawerAdapter(private val items: List<DrawerItem>) :
    RecyclerView.Adapter<DrawerAdapter.ViewHolder>() {

    var clickListener: View.OnClickListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleLayout: LinearLayout = view.findViewById(R.id.title_layout)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val status: TextView = view.findViewById(R.id.status)
        val flexboxTags: FlexboxLayout = view.findViewById(R.id.flexboxTags)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drawer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title

        // 动态加载子标签
        if (item.isExpanded) {
            holder.flexboxTags.visibility = View.VISIBLE
            holder.status.rotation = 90f
            if (holder.flexboxTags.childCount == 0){
                item.tags.forEach { tag ->
                    val textView = TextView(holder.itemView.context).apply {
                        text = tag.getString("title")
                        setTextColor(Color.BLACK)
                        setPadding(16, 12, 16, 12)
                        textSize = 15F
                        background = ContextCompat.getDrawable(context, R.drawable.rounded_corner_selector)
                    }

                    //设置样式
                    textView.layoutParams = FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(20, 18, 20, 18)
                        tag.getObject("style",FlexChildStyle::class.java).apply(this)
                    }

                    textView.isClickable = true
                    textView.gravity = Gravity.CENTER
                    val url = tag.getString("url")
                    if (url != null && url.isNotEmpty()){
                        textView.setTag(R.id.tv_title,Pair(url,item.source))
                        textView.setOnClickListener(clickListener)
                    }

                    holder.flexboxTags.addView(textView)

                }
            }
        } else {
            holder.flexboxTags.visibility = View.GONE
            holder.status.rotation = 0f
        }

        // 点击标题切换展开状态
        holder.titleLayout.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = items.size
}