package hys.hmonkeyys.readimagetext

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import hys.hmonkeyys.readimagetext.adapter.HistoryAdapter
import hys.hmonkeyys.readimagetext.databinding.ActivityHistoryBinding
import hys.hmonkeyys.readimagetext.model.WebHistoryModel
import hys.hmonkeyys.readimagetext.room.WebDatabase
import kotlinx.coroutines.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    private var db: WebDatabase? = null

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = WebDatabase.getInstance(applicationContext)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.deleteAllButton.setOnClickListener {
            // todo 다이얼로그로 확인 받고 삭제하기
            CoroutineScope(Dispatchers.IO).launch {
                db?.historyDao()?.deleteAll()
            }
        }

        initAdapter()
    }

    private fun initAdapter() {
        historyAdapter = HistoryAdapter(selectedDeleteListener = {
            CoroutineScope(Dispatchers.IO).launch {
                db?.historyDao()?.delete(it)
            }
            historyAdapter.notifyDataSetChanged()
            /*CoroutineScope(Dispatchers.IO).launch {
                val deferredFirst = CoroutineScope(Dispatchers.IO).async { db?.historyDao()?.delete(it) }
                val deferredSecond = CoroutineScope(Dispatchers.IO).async { historyAdapter.submitList(db?.historyDao()?.getAll()) }
                deferredFirst.await()
                deferredSecond.await()
            }*/

        })

        binding.historyRecyclerView.adapter = historyAdapter

        CoroutineScope(Dispatchers.IO).launch {
            historyAdapter.submitList(db?.historyDao()?.getAll())
        }
    }

}