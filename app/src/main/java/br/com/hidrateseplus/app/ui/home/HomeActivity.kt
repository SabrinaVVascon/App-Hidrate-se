package br.com.hidrateseplus.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import br.com.hidrateseplus.app.databinding.ActivityMainBinding
import br.com.hidrateseplus.app.ui.history.HistoryActivity
import br.com.hidrateseplus.app.ui.settings.SettingsActivity
import br.com.hidrateseplus.data.local.AppDatabase
import br.com.hidrateseplus.data.local.LocalDataSource
import br.com.hidrateseplus.data.remote.RemoteDataSource
import br.com.hidrateseplus.data.repository.WaterRepository

// ============================================================
// MVVM — View (Activity)
// Responsabilidade: observar o estado e reagir à UI.
// NÃO contém lógica de negócio, NÃO acessa banco diretamente.
// ============================================================
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Instancia ViewModel com Factory (injeção manual do Repository)
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(buildRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega dados quando volta da tela de configurações
        viewModel.loadData(getSavedGoal())
    }

    // ── Observers: reagem a mudanças no estado da ViewModel ──────────

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            renderState(state)
        }

        viewModel.snackbarMessage.observe(this) { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                viewModel.onSnackbarShown()
            }
        }
    }

    // Atualiza toda a UI com base no estado imutável recebido
    private fun renderState(state: HomeUiState) {
        val safeGoal = if (state.goalMl > 0) state.goalMl else 1

        binding.tvTotal.text = "${state.totalMl} ml"
        binding.tvGoal.text = if (state.goalMl > 0) "Meta diária: ${state.goalMl} ml" else "Meta diária: --"
        binding.progressGoal.max = safeGoal
        binding.progressGoal.progress = state.totalMl.coerceAtMost(safeGoal)

        // Exibe erro se houver
        if (state.errorMessage != null) {
            Snackbar.make(binding.root, state.errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    // ── Click Listeners: delegam ações para a ViewModel ─────────────

    private fun setupClickListeners() {
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnAdd250.setOnClickListener {
            viewModel.addWater(250)
        }

        binding.btnAdd500.setOnClickListener {
            viewModel.addWater(500)
        }

        binding.btnAddCustom.setOnClickListener {
            val value = binding.etCustomMl.text.toString().trim().toIntOrNull()
            if (value != null && value > 0) {
                viewModel.addWater(value)
                binding.etCustomMl.text?.clear()
            } else {
                binding.etCustomMl.error = "Digite um valor válido"
            }
        }

        binding.btnUndo.setOnClickListener {
            viewModel.undoLastEntry()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private fun getSavedGoal(): Int {
        val prefs = getSharedPreferences("hydratese_prefs", MODE_PRIVATE)
        return prefs.getInt("daily_goal_ml", 2000)
    }

    // Constrói o grafo de dependências manualmente (sem Hilt/Koin)
    private fun buildRepository(): WaterRepository {
        val db = AppDatabase.getDatabase(this)
        val localDataSource = LocalDataSource(db.waterDao())

        // ✅ Alterado para Firebase (sem parâmetro)
        val remoteDataSource = RemoteDataSource()

        return WaterRepository(localDataSource, remoteDataSource)
    }
}