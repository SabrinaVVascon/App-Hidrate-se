package br.com.hidrateseplus.app.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.hidrateseplus.app.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = HistoryAdapter(
            listOf(
                HistoryDay("24/02/2026", "1750 ml"),
                HistoryDay("23/02/2026", "2000 ml"),
                HistoryDay("22/02/2026", "1250 ml")
            )
        )
    }
}