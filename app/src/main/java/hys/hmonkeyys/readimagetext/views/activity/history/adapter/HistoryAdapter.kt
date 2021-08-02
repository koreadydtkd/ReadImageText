package hys.hmonkeyys.readimagetext.views.activity.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.databinding.ItemDateBinding
import hys.hmonkeyys.readimagetext.databinding.ItemWebBinding
import hys.hmonkeyys.readimagetext.utils.setOnDuplicatePreventionClickListener

class HistoryAdapter(
    val deleteSelectItemListener: (Int, String) -> Unit,
    val moveWebView: (String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var historyList = mutableListOf<HistoryType>()

    inner class UrlViewHolder(private val binding: ItemWebBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UrlType?) {
            item ?: return
            item.loadUrl?.let { loadUrl ->
                binding.historyUrlTextView.text = loadUrl
                binding.historyUrlTextView.setOnDuplicatePreventionClickListener {
                    moveWebView(loadUrl)
                }

                binding.deleteButton.setOnDuplicatePreventionClickListener {
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

    inner class DateViewHolder(private val binding: ItemDateBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DateType?) {
            item ?: return
            binding.dateTextView.text = item.date
        }
    }

    fun setHistoryList(list: MutableList<HistoryType>) {
        this.historyList = list
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HistoryType.DATE) {
            val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DateViewHolder(binding)
        } else {
            val binding = ItemWebBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            UrlViewHolder(binding)
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

    override fun getItemCount(): Int = historyList.size

    override fun getItemViewType(position: Int): Int = historyList[position].type
}