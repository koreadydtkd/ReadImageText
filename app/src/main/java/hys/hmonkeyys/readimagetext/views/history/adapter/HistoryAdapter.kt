package hys.hmonkeyys.readimagetext.views.history.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.utils.setOnDuplicatePreventionClickListener

class HistoryAdapter(
    val deleteSelectItemListener: (Int, String) -> Unit,
    val moveWebView: (String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var historyList = mutableListOf<HistoryType>()

    inner class UrlViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val historyUrlTextView: TextView = view.findViewById(R.id.historyUrlTextView)
        private val deleteButton: CardView = view.findViewById(R.id.deleteButton)

        fun bind(item: UrlType?) {
            item ?: return
            item.loadUrl?.let { loadUrl ->
                historyUrlTextView.apply {
                    text = loadUrl
                    setOnDuplicatePreventionClickListener {
                        moveWebView(loadUrl)
                    }
                }

                deleteButton.setOnDuplicatePreventionClickListener {
                    try {
                        // 이전, 다음 아이템이 날짜타입인 경우
                        if (historyList[layoutPosition - 1].type == HistoryType.DATE &&
                            historyList[layoutPosition + 1].type == HistoryType.DATE
                        ) {
                            historyList.removeAt(layoutPosition)
                            historyList.removeAt(layoutPosition - 1)
                        } else {
                            historyList.removeAt(layoutPosition)
                        }
                    } catch (e: IndexOutOfBoundsException) {
                        // 마지막 아이템 삭제 시 이전 아이템이 날짜인 경우 에러 발생 처리
                        historyList.removeAt(layoutPosition)
                        historyList.removeAt(layoutPosition - 1)
                    } finally {
                        deleteSelectItemListener(item.uid, loadUrl)
                    }
                }

            }

        }
    }

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateTextView: TextView = view.findViewById(R.id.dateTextView)

        fun bind(item: DateType?) {
            item ?: return
            dateTextView.text = item.date
        }
    }

    fun setHistoryList(list: MutableList<HistoryType>) {
        this.historyList = list
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HistoryType.DATE) {
            DateViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false))
        } else {
            UrlViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_web, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UrlViewHolder -> {
                holder.bind(historyList[position] as? UrlType)
            }
            is DateViewHolder -> {
                holder.bind(historyList[position] as? DateType)
            }
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun getItemViewType(position: Int): Int {
        return historyList[position].type
    }
}