package hys.hmonkeyys.readimagetext.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.model.WebHistoryModel

class HistoryAdapter2 (
    val deleteSelectItemListener: (WebHistoryModel) -> Unit,
    val moveWebView: (String) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var historyList = mutableListOf<HistoryType>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val historyUrlTextView: TextView = view.findViewById(R.id.historyUrlTextView)
        private val deleteButton: CardView = view.findViewById(R.id.deleteButton)

        fun bind(item: AddressType?) {
            item?.let{
                historyUrlTextView.apply {
                    text = it.loadUrl
                    setOnClickListener {
                        item.loadUrl ?: return@setOnClickListener
//                        moveWebView(it.loadUrl)
                    }
                }

                deleteButton.setOnClickListener {
//                    if (item.loadUrl == null || item.visitDate == null) {
//                        return@setOnClickListener
//                    }
//                    deleteSelectItemListener(item)
                }
            }
        }
    }

    inner class DateViewHolder(view : View) : RecyclerView.ViewHolder(view){
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        fun bind(item : DateType?){
            item?.let{
                dateTextView.text = it.date
            }
        }
    }

    fun setHistoryList(list: MutableList<HistoryType>) {
        list.forEach {
            Log.e("History Type", "${it.type}")
            if(it is DateType){
                Log.e("Date Type", "${it.date}")
            }else if(it is AddressType){
                Log.e("Address Type", "${it.loadUrl}")
            }
        }
        this.historyList = list
        this.notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return historyList[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == HistoryType.DATE){
            DateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false))
        }else{
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_web, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder){
            holder.bind(historyList[position] as? AddressType)
        }else if(holder is DateViewHolder){
            holder.bind(historyList[position] as? DateType)
        }
    }

    override fun getItemCount(): Int = historyList.size
}