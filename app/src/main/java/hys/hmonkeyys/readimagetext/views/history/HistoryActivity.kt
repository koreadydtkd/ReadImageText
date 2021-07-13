package hys.hmonkeyys.readimagetext.views.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.history.adapter.HistoryAdapter
import hys.hmonkeyys.readimagetext.views.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


internal class HistoryActivity : BaseActivity<HistoryViewModel>(

) {
    private val binding: ActivityHistoryBinding by lazy {
        ActivityHistoryBinding.inflate(layoutInflater)
    }

    override val viewModel: HistoryViewModel by viewModel()

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun observeData() {
        viewModel.historyStateData.observe(this) {
            when(it) {
                is HistoryState.Initialized -> {
                    initStatusBar()
                    initViews()
                    initAdapter()
                }
                is HistoryState.GetHistoryData -> {
                    historyAdapter.setHistoryList(it.historyList)
                }
                is HistoryState.Delete -> {
                    if(it.isAll) {
                        noData()
                    } else {
                        historyAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun initStatusBar() {
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.teal_200, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.deleteAllButton.setOnClickListener {
            showDeleteDialog()
        }
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

        viewModel.getAll()
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
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra(Util.MAIN_TO_HISTORY_DEFAULT, url)
        startActivity(intent)
    }

    private fun noData() {
        binding.textView.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE

        binding.noDataTextView.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "HYS_HistoryActivity"

        private const val ALL = "all"
    }
}