package br.com.hidrateseplus.app.ui.history

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import br.com.hidrateseplus.app.databinding.ActivityHistoryBinding
import br.com.hidrateseplus.data.local.AppDatabase
import br.com.hidrateseplus.data.local.LocalDataSource
import br.com.hidrateseplus.data.remote.RemoteDataSource
import br.com.hidrateseplus.data.remote.RetrofitClient
import br.com.hidrateseplus.data.repository.WaterRepository

// ============================================================
// MVVM — View da tela de Histórico
// Responsabilidade: observar lista de dias e renderizar.
// Dados reais do banco — sem hardcode.
// ============================================================
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(buildRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.historyItems.observe(this) { items ->
            binding.rvHistory.adapter = HistoryAdapter(items)
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.onErrorShown()
            }
        }
    }

    private fun buildRepository(): WaterRepository {
        val db = AppDatabase.getDatabase(this)
        val localDataSource = LocalDataSource(db.waterDao())
        val remoteDataSource = RemoteDataSource(RetrofitClient.apiService)
        return WaterRepository(localDataSource, remoteDataSource)
    }
}