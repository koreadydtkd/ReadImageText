package hys.hmonkeyys.readimagetext.floating

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.adapter.HistoryAdapter
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.model.WebHistoryModel
import hys.hmonkeyys.readimagetext.room.WebDatabase
import hys.hmonkeyys.readimagetext.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        initViews()
        initAdapter()
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
        historyAdapter = HistoryAdapter(deleteSelectItemListener = { selectModel ->
            deleteHistory(selectModel)
        }, moveWebView = { selectUrl ->
            setResult(RESPONSE_CODE, Intent().putExtra(Util.MAIN_TO_HISTORY_DEFAULT, selectUrl))
            finish()
        })

        binding.historyRecyclerView.adapter = historyAdapter

        CoroutineScope(Dispatchers.IO).launch {
            historyAdapter.submitList(db?.historyDao()?.getAll())
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle(resources.getString(R.string.history_dialog_title))
            .setMessage(resources.getString(R.string.history_dialog_message))
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteHistory(null)
            }.setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun deleteHistory(selectModel: WebHistoryModel?) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Default) {
                if(selectModel == null) {
                    db?.historyDao()?.deleteAll()
                    Log.d(TAG, "모두 삭제")
                } else {
                    db?.historyDao()?.delete(selectModel)
                    Log.d(TAG, "선택 삭제")
                }
                historyAdapter.submitList(db?.historyDao()?.getAll())
            }
        }
    }

    companion object {
        private const val TAG = "HYS_HistoryActivity"
        private const val RESPONSE_CODE = 1014
    }
}