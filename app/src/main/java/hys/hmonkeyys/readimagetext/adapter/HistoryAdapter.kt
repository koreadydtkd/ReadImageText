package hys.hmonkeyys.readimagetext.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.model.WebHistoryModel

class HistoryAdapter (
    val selectedDeleteListener: (WebHistoryModel) -> Unit
): ListAdapter<WebHistoryModel, HistoryAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: WebHistoryModel) {
            view.findViewById<TextView>(R.id.historyUrlTextView).apply {
                text = item.loadUrl
                setOnClickListener {

                }
            }
            view.findViewById<TextView>(R.id.visitDateTextView).text = item.visitDate

            view.findViewById<View>(R.id.deleteButton).setOnClickListener {
                if (item.loadUrl == null || item.visitDate == null) {
                    return@setOnClickListener
                }
                selectedDeleteListener(item)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_web, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<WebHistoryModel>() {
            override fun areItemsTheSame(oldItem: WebHistoryModel, newItem: WebHistoryModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: WebHistoryModel, newItem: WebHistoryModel): Boolean {
                return oldItem == newItem
            }
        }
    }

}