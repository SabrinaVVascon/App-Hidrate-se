package br.com.hidrateseplus.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.hidrateseplus.app.databinding.ActivityMainBinding
import br.com.hidrateseplus.app.ui.history.HistoryActivity
import br.com.hidrateseplus.app.ui.settings.SettingsActivity
import br.com.hidrateseplus.data.local.AppDatabase
import br.com.hidrateseplus.data.local.WaterDao
import br.com.hidrateseplus.data.local.WaterEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var waterDao: WaterDao

    private var totalMl = 0
    private var goalMl = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        waterDao = AppDatabase.getDatabase(this).waterDao()

        updateUI()
        loadHomeData()

        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnAdd250.setOnClickListener {
            addWater(250)
        }

        binding.btnAdd500.setOnClickListener {
            addWater(500)
        }

        binding.btnAddCustom.setOnClickListener {
            val value = binding.etCustomMl.text.toString().trim().toIntOrNull()

            if (value != null && value > 0) {
                addWater(value)
                binding.etCustomMl.text?.clear()
            } else {
                binding.etCustomMl.error = "Digite um valor válido"
            }
        }

        binding.btnUndo.setOnClickListener {
            undoLastEntry()
        }
    }

    override fun onResume() {
        super.onResume()
        loadHomeData()
    }

    private fun loadHomeData() {
        lifecycleScope.launch {
            goalMl = getSavedGoal()

            totalMl = withContext(Dispatchers.IO) {
                waterDao.getTodayTotal(getTodayDate())
            }

            updateUI()
        }
    }

    private fun addWater(ml: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                waterDao.insert(
                    WaterEntryEntity(
                        amount = ml,
                        date = getTodayDate()
                    )
                )
            }

            loadHomeData()
        }
    }

    private fun undoLastEntry() {
        lifecycleScope.launch {
            val lastEntry = withContext(Dispatchers.IO) {
                waterDao.getLastEntry(getTodayDate())
            }

            if (lastEntry != null) {
                withContext(Dispatchers.IO) {
                    waterDao.deleteById(lastEntry.id)
                }

                loadHomeData()
            }
        }
    }

    private fun updateUI() {
        binding.tvTotal.text = "${totalMl} ml"

        val safeGoal = if (goalMl > 0) goalMl else 1
        binding.progressGoal.max = safeGoal
        binding.progressGoal.progress = totalMl.coerceAtMost(safeGoal)

        binding.tvGoal.text = if (goalMl > 0) {
            "Meta diária: ${goalMl} ml"
        } else {
            "Meta diária: --"
        }
    }

    private fun getSavedGoal(): Int {
        val prefs = getSharedPreferences("hydratese_prefs", MODE_PRIVATE)
        return prefs.getInt("daily_goal_ml", 0)
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}