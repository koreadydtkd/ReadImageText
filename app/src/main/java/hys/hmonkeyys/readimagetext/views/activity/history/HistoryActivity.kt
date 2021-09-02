package hys.hmonkeyys.readimagetext.views.activity.history

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.utils.Utility.MAIN_TO_HISTORY_DEFAULT
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.activity.history.adapter.HistoryAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


internal class HistoryActivity : BaseActivity<HistoryViewModel>(

) {
    private val binding: ActivityHistoryBinding by lazy { ActivityHistoryBinding.inflate(layoutInflater) }

    private lateinit var historyAdapter: HistoryAdapter

    override val viewModel: HistoryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun observeData() {
        viewModel.historyStateData.observe(this) {
            when (it) {
                is HistoryState.Initialized -> { // 초기화 작업
                    initViews()
                    initAdapter()
                }
                is HistoryState.GetHistoryData -> { // 방문기록(Room DB) 불러오기
                    historyAdapter.setHistoryList(it.historyList)
                }
                is HistoryState.Delete -> { // 방문기록 삭제(All or Select)
                    if (it.isAll) {
                        changeView()
                    } else {
                        historyAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }


    private fun initViews() {
        binding.backButton.setOnDuplicatePreventionClickListener { finish() }
        binding.deleteAllButton.setOnDuplicatePreventionClickListener { showDeleteDialog() }
    }

    private fun initAdapter() {
        historyAdapter = HistoryAdapter(
            deleteSelectItemListener = { uid, loadUrl ->
                viewModel.deleteHistory(uid, loadUrl)
            },
            moveWebView = { selectUrl ->
                selectURLGoToMainWebView(selectUrl)
            }
        )
        binding.historyRecyclerView.adapter = historyAdapter
        viewModel.getAllHistory()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.history_dialog_title))
            .setMessage(resources.getString(R.string.history_dialog_message))
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                viewModel.deleteHistory(0, ALL)
            }.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun selectURLGoToMainWebView(url: String) {
        Log.i(TAG, url)
        val intent = Intent()
        intent.putExtra(MAIN_TO_HISTORY_DEFAULT, url)
        setResult(200, intent)
        finish()
    }

    private fun changeView() {
        binding.textView.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE

        binding.noDataTextView.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "HYS_HistoryActivity"
        private const val ALL = "all"
    }
}