package hys.hmonkeyys.readimagetext

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import hys.hmonkeyys.readimagetext.adapter.HistoryAdapter
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
        historyAdapter = HistoryAdapter(
            deleteSelectItemListener = { selectModel ->
                deleteHistory(selectModel)
            },
            moveWebView = { selectUrl ->
                selectURLGoToMainWebView(selectUrl)
            }
        )

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
            if(selectModel == null) {
                db?.historyDao()?.deleteAll()
                Log.i(TAG, "모두 삭제")
            } else {
                db?.historyDao()?.delete(selectModel)
                Log.i(TAG, "선택 삭제")
            }
            historyAdapter.submitList(db?.historyDao()?.getAll())
        }.invokeOnCompletion {
            Log.i(TAG, "삭제 완료")
            Handler(mainLooper).postDelayed({
                historyAdapter.notifyDataSetChanged()
            }, 300)

//            Thread.sleep(300)
//            runOnUiThread {
//                historyAdapter.notifyDataSetChanged()
//            }
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