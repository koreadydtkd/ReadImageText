package hys.hmonkeyys.readimagetext.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.model.WebHistoryModel

class HistoryAdapter (
    val deleteSelectItemListener: (WebHistoryModel) -> Unit,
    val moveWebView: (String) -> Unit
): ListAdapter<WebHistoryModel, HistoryAdapter.ViewHolder>(diffUtil) {

    var duplicateVisitDate = ""

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        private val historyUrlTextView: TextView = view.findViewById(R.id.historyUrlTextView)
        private val deleteButton: CardView = view.findViewById(R.id.deleteButton)

        fun bind(item: WebHistoryModel) {

            if(duplicateVisitDate != item.visitDate) {
                duplicateVisitDate = item.visitDate.toString()
                dateTextView.text = duplicateVisitDate
                dateTextView.visibility = View.VISIBLE
            } else {
                dateTextView.visibility = View.GONE
            }

            historyUrlTextView.apply {
                text = item.loadUrl
                setOnClickListener {
                    item.loadUrl ?: return@setOnClickListener
                    moveWebView(item.loadUrl)
                }
            }

            deleteButton.setOnClickListener {
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

    override fun onCurrentListChanged(previousList: MutableList<WebHistoryModel>, currentList: MutableList<WebHistoryModel>) {
        super.onCurrentListChanged(previousList, currentList)
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