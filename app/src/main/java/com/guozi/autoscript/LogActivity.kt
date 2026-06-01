package com.guozi.autoscript

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LogActivity : AppCompatActivity() {
    private lateinit var tvLogSummary: TextView
    private lateinit var tvFullLog: TextView
    private lateinit var scrollLog: ScrollView
    private var lastRenderedCount = -1

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            renderLogs()
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        title = "全部日志"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvLogSummary = findViewById(R.id.tvLogSummary)
        tvFullLog = findViewById(R.id.tvFullLog)
        scrollLog = findViewById(R.id.scrollFullLog)

        findViewById<Button>(R.id.btnClearAllLogs).setOnClickListener {
            LogStore.clear()
            renderLogs(scrollToBottom = false)
        }

        renderLogs(scrollToBottom = true)
    }

    override fun onResume() {
        super.onResume()
        refreshHandler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun renderLogs(scrollToBottom: Boolean = false) {
        val logs = LogStore.all()
        val count = logs.size
        if (count == lastRenderedCount && !scrollToBottom) return

        tvLogSummary.text = "共 $count 条日志"
        tvFullLog.text = if (logs.isEmpty()) {
            "暂无日志"
        } else {
            logs.joinToString(separator = "\n", postfix = "\n")
        }

        if (scrollToBottom || count > lastRenderedCount) {
            scrollLog.post {
                scrollLog.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
        lastRenderedCount = count
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 1000L
    }
}
