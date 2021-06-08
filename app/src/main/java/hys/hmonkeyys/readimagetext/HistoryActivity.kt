package hys.hmonkeyys.readimagetext

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hys.hmonkeyys.readimagetext.adapter.HistoryAdapter
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.model.WebHistoryModel
import hys.hmonkeyys.readimagetext.room.WebDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    private var db: WebDatabase? = null

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
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
            setResult(RESPONSE_CODE, Intent().putExtra("select_url", selectUrl))
            finish()
        })

        binding.historyRecyclerView.adapter = historyAdapter

        CoroutineScope(Dispatchers.IO).launch {
            historyAdapter.submitList(db?.historyDao()?.getAll())
        }
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("히스토리 모두 삭제")
            .setMessage("방문 기록을 모두 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteHistory(null)
            }.setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun deleteHistory(selectModel: WebHistoryModel?) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Default) {
                if(selectModel == null) {
                    db?.historyDao()?.deleteAll()
                } else {
                    db?.historyDao()?.delete(selectModel)
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