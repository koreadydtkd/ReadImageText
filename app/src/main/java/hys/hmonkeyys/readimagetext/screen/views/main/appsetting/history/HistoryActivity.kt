package hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.screen.BaseActivity
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.adapter.HistoryAdapter
import hys.hmonkeyys.readimagetext.utils.Constant.MAIN_TO_HISTORY_DEFAULT

@AndroidEntryPoint
internal class HistoryActivity : BaseActivity<HistoryViewModel, ActivityHistoryBinding>() {

    override val viewModel: HistoryViewModel by viewModels()
    override fun getViewBinding(): ActivityHistoryBinding = ActivityHistoryBinding.inflate(layoutInflater)

    private lateinit var adapter: HistoryAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun observeData() {
        viewModel.historyStateData.observe(this) {
            when (it) {
                is HistoryState.Initialized -> viewModel.getAllHistory()

                is HistoryState.GetHistoryData -> adapter.setHistoryList(it.historyList)

                is HistoryState.Delete -> adapter.notifyDataSetChanged()

                is HistoryState.DeleteAll -> historyDeleteAll()
            }
        }
    }

    /** 뷰 초기화 */
    override fun initViews() = with(binding) {
        backButton.setOnDuplicatePreventionClickListener { finish() }
        deleteAllButton.setOnDuplicatePreventionClickListener { showDeleteDialog() }

        adapter = HistoryAdapter(
            deleteSelectItemListener = { uid, loadUrl ->
                viewModel.deleteHistory(uid, loadUrl)
            },
            moveWebView = { selectUrl ->
                selectURLGoToMainWebView(selectUrl)
            }
        )
        historyRecyclerView.adapter = adapter
    }

    /** 방문기록 삭제 다이얼로그 보여주기 */
    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_dialog_title))
            .setMessage(getString(R.string.history_dialog_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteHistory(0, ALL)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    /** 선택 URL 이동 */
    private fun selectURLGoToMainWebView(url: String) {
        val intent = Intent().apply {
            putExtra(MAIN_TO_HISTORY_DEFAULT, url)
        }
        setResult(200, intent)
        finish()
    }

    /** 데이터 모두 삭제 */
    private fun historyDeleteAll() {
        binding.textView.isGone = true
        binding.historyRecyclerView.isGone = true
        binding.noDataTextView.isVisible = true
    }

    companion object {
//        private const val TAG = "HYS_HistoryActivity"
        private const val ALL = "all"
    }
}