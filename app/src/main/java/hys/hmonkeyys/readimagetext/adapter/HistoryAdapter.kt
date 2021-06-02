package hys.hmonkeyys.readimagetext.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.model.WebHistoryModel

class HistoryAdapter (
    val deleteSelectItemListener: (WebHistoryModel) -> Unit,
    val moveWebView: (String) -> Unit
): ListAdapter<WebHistoryModel, HistoryAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: WebHistoryModel) {
            view.findViewById<TextView>(R.id.historyUrlTextView).apply {
                text = item.loadUrl
                setOnClickListener {
                    item.loadUrl ?: return@setOnClickListener
                    moveWebView(item.loadUrl)
                }
            }

            view.findViewById<View>(R.id.deleteButton).setOnClickListener {
                if (item.loadUrl == null || item.visitDate == null) {
                    return@setOnClickListener
                }
                deleteSelectItemListener(item)
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
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: WebHistoryModel, newItem: WebHistoryModel): Boolean {
                return oldItem == newItem
            }
        }
    }

}