package hys.hmonkeyys.readimagetext

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hys.hmonkeyys.readimagetext.adapter.*
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.model.WebHistoryModel
import hys.hmonkeyys.readimagetext.room.WebDatabase
import hys.hmonkeyys.readimagetext.utils.Util
import kotlinx.coroutines.*


class HistoryActivity : AppCompatActivity() {
    private val binding: ActivityHistoryBinding by lazy {
        ActivityHistoryBinding.inflate(layoutInflater)
    }

    private var db: WebDatabase? = null

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = WebDatabase.getInstance(applicationContext)

        initStatusBar()
        initViews()
        initAdapter()
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
                deleteHistory(uid, loadUrl)
            },
            moveWebView = { selectUrl ->
                selectURLGoToMainWebView(selectUrl)
            }
        )

        binding.historyRecyclerView.adapter = historyAdapter

        CoroutineScope(Dispatchers.IO).launch {
            db?.historyDao()?.getAll()?.let {
                historyAdapter.setHistoryList(convertList(it))
            }
        }
    }

    private fun convertList(list: MutableList<WebHistoryModel>): MutableList<HistoryType>{
        var prefDate : String? = ""
        val historyList = mutableListOf<HistoryType>()

        list.forEach { webHistoryModel ->
            // 날짜 영역
            if(webHistoryModel.visitDate != prefDate) {
                historyList.add(
                    DateType(HistoryType.DATE).apply {
                        date = webHistoryModel.visitDate
                    }
                )
                prefDate = webHistoryModel.visitDate
            }

            // 방문 이력
            historyList.add(
                AddressType(HistoryType.ADDRESS).apply {
                    loadUrl = webHistoryModel.loadUrl
                    webHistoryModel.uid?.let {
                        uid = it
                    }

                }
            )
        }

        return historyList
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.history_dialog_title))
            .setMessage(resources.getString(R.string.history_dialog_message))
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteHistory(0, "ALL")
            }.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun deleteHistory(uid: Int, loadUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if(uid == 0 && loadUrl == "ALL") {
                db?.historyDao()?.deleteAll()
                Log.i(TAG, "모두 삭제")
            } else {
                db?.historyDao()?.deleteSelectedItem(uid, loadUrl)
                Log.i(TAG, "선택 삭제")
            }
        }
    }

    private fun selectURLGoToMainWebView(url: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra(Util.MAIN_TO_HISTORY_DEFAULT, url)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "HYS_HistoryActivity"
    }
}