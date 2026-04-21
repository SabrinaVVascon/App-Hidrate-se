package br.com.hidrateseplus.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.com.hidrateseplus.app.databinding.ActivitySettingsBinding
import br.com.hidrateseplus.app.worker.WaterReminderWorker
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var calculatedGoalMl: Int? = null
    private var selectedGoalType: String? = null
    private var isLoadingSettings = false

    private val prefs by lazy {
        getSharedPreferences("hydratese_prefs", MODE_PRIVATE)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                scheduleReminderWork(getSelectedFrequency())
                Toast.makeText(this, "Lembretes ativados", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Permissão de notificação negada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupExpandableSections()
        setupFrequencySpinner()
        setupReminderSwitch()
        setupGoalInputs()
        loadSavedSettings()
        setupButtons()
    }

    private fun setupExpandableSections() {
        binding.headerCalculatedGoal.setOnClickListener {
            toggleSection(binding.layoutCalculatedGoal, binding.tvToggleCalculated)
        }

        binding.headerManualGoal.setOnClickListener {
            toggleSection(binding.layoutManualGoal, binding.tvToggleManual)
        }
    }

    private fun toggleSection(layout: View, toggleText: TextView) {
        val isVisible = layout.visibility == View.VISIBLE

        if (isVisible) {
            layout.visibility = View.GONE
            toggleText.text = "+"
        } else {
            layout.visibility = View.VISIBLE
            toggleText.text = "−"
        }
    }

    private fun setupFrequencySpinner() {
        val frequencies = listOf("30 min", "1 hora", "2 horas", "3 horas")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            frequencies
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrequency.adapter = adapter
    }

    private fun setupReminderSwitch() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutFrequency.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setupGoalInputs() {
        binding.etGoal.doAfterTextChanged { text ->
            if (!isLoadingSettings && !text.isNullOrBlank()) {
                selectedGoalType = GOAL_TYPE_MANUAL
            }
        }

        binding.etWeight.doAfterTextChanged {
            if (!isLoadingSettings) {
                binding.tvCalculatedGoal.text = "Meta diária (ml): --"
                calculatedGoalMl = null
            }
        }
    }

    private fun loadSavedSettings() {
        isLoadingSettings = true

        val remindersEnabled = prefs.getBoolean(KEY_REMINDERS_ENABLED, false)
        val savedFrequency = prefs.getString(KEY_REMINDER_FREQUENCY, "1 hora") ?: "1 hora"

        val savedManualGoal = prefs.getInt(KEY_MANUAL_GOAL_ML, 0)
        val savedCalculatedGoal = prefs.getInt(KEY_CALCULATED_GOAL_ML, 0)
        val savedCalculatedWeight = prefs.getString(KEY_CALCULATED_WEIGHT, "") ?: ""
        val savedGoalType = prefs.getString(KEY_GOAL_TYPE, null)

        binding.etGoal.setText("")
        binding.etWeight.setText("")
        binding.tvCalculatedGoal.text = "Meta diária (ml): --"
        calculatedGoalMl = null

        when (savedGoalType) {
            GOAL_TYPE_MANUAL -> {
                if (savedManualGoal > 0) {
                    binding.etGoal.setText(savedManualGoal.toString())
                }
                binding.layoutManualGoal.visibility = View.VISIBLE
                binding.tvToggleManual.text = "−"
            }

            GOAL_TYPE_CALCULATED -> {
                if (savedCalculatedGoal > 0) {
                    calculatedGoalMl = savedCalculatedGoal
                    binding.tvCalculatedGoal.text = "Meta diária (ml): $savedCalculatedGoal"
                }
                if (savedCalculatedWeight.isNotBlank()) {
                    binding.etWeight.setText(savedCalculatedWeight)
                }
                binding.layoutCalculatedGoal.visibility = View.VISIBLE
                binding.tvToggleCalculated.text = "−"
            }
        }

        binding.switchReminder.isChecked = remindersEnabled
        binding.layoutFrequency.visibility = if (remindersEnabled) View.VISIBLE else View.GONE

        val frequencies = listOf("30 min", "1 hora", "2 horas", "3 horas")
        val savedIndex = frequencies.indexOf(savedFrequency)
        if (savedIndex >= 0) {
            binding.spinnerFrequency.setSelection(savedIndex)
        }

        isLoadingSettings = false
    }

    private fun setupButtons() {
        binding.btnCalculateGoal.setOnClickListener {
            val weightText = binding.etWeight.text.toString().trim()

            if (weightText.isEmpty()) {
                Toast.makeText(this, "Digite o peso para calcular", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toDoubleOrNull()

            if (weight == null || weight <= 0) {
                Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            calculatedGoalMl = (weight * 35).toInt()
            selectedGoalType = GOAL_TYPE_CALCULATED
            binding.tvCalculatedGoal.text = "Meta diária (ml): $calculatedGoalMl"
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        val currentGoalType = prefs.getString(KEY_GOAL_TYPE, null)
        val currentGoalValue = prefs.getInt(KEY_DAILY_GOAL_ML, 0)

        val manualGoal = binding.etGoal.text.toString().trim().toIntOrNull()
        val weightText = binding.etWeight.text.toString().trim()
        val remindersEnabled = binding.switchReminder.isChecked
        val frequency = getSelectedFrequency()

        val goalTypeToSave = when (selectedGoalType) {
            GOAL_TYPE_MANUAL -> {
                if (manualGoal == null || manualGoal <= 0) {
                    Toast.makeText(this, "Digite uma meta manual válida", Toast.LENGTH_SHORT).show()
                    return
                }
                GOAL_TYPE_MANUAL
            }

            GOAL_TYPE_CALCULATED -> {
                if (calculatedGoalMl == null || calculatedGoalMl!! <= 0) {
                    Toast.makeText(this, "Calcule a meta antes de salvar", Toast.LENGTH_SHORT).show()
                    return
                }
                GOAL_TYPE_CALCULATED
            }

            else -> currentGoalType
        }

        val goalValueToSave = when (goalTypeToSave) {
            GOAL_TYPE_MANUAL -> manualGoal ?: currentGoalValue
            GOAL_TYPE_CALCULATED -> calculatedGoalMl ?: currentGoalValue
            else -> currentGoalValue
        }

        if (
            selectedGoalType != null &&
            currentGoalType != null &&
            currentGoalType != goalTypeToSave &&
            currentGoalValue > 0
        ) {
            showOverwriteDialog(
                oldType = currentGoalType,
                newValue = goalValueToSave,
                onConfirm = {
                    persistSettings(
                        goalTypeToSave = goalTypeToSave,
                        goalValueToSave = goalValueToSave,
                        manualGoal = manualGoal,
                        calculatedGoal = calculatedGoalMl,
                        calculatedWeight = weightText,
                        remindersEnabled = remindersEnabled,
                        frequency = frequency
                    )
                }
            )
            return
        }

        persistSettings(
            goalTypeToSave = goalTypeToSave,
            goalValueToSave = goalValueToSave,
            manualGoal = manualGoal,
            calculatedGoal = calculatedGoalMl,
            calculatedWeight = weightText,
            remindersEnabled = remindersEnabled,
            frequency = frequency
        )
    }

    private fun persistSettings(
        goalTypeToSave: String?,
        goalValueToSave: Int,
        manualGoal: Int?,
        calculatedGoal: Int?,
        calculatedWeight: String,
        remindersEnabled: Boolean,
        frequency: String
    ) {
        val editor = prefs.edit()
            .putInt(KEY_DAILY_GOAL_ML, goalValueToSave)
            .putString(KEY_GOAL_TYPE, goalTypeToSave)
            .putBoolean(KEY_REMINDERS_ENABLED, remindersEnabled)
            .putString(KEY_REMINDER_FREQUENCY, frequency)

        when (goalTypeToSave) {
            GOAL_TYPE_MANUAL -> {
                editor
                    .putInt(KEY_MANUAL_GOAL_ML, manualGoal ?: 0)
                    .putInt(KEY_CALCULATED_GOAL_ML, 0)
                    .putString(KEY_CALCULATED_WEIGHT, "")
            }

            GOAL_TYPE_CALCULATED -> {
                editor
                    .putInt(KEY_MANUAL_GOAL_ML, 0)
                    .putInt(KEY_CALCULATED_GOAL_ML, calculatedGoal ?: 0)
                    .putString(KEY_CALCULATED_WEIGHT, calculatedWeight)
            }
        }

        editor.apply()

        if (remindersEnabled) {
            requestPermissionAndScheduleReminder(frequency)
        } else {
            cancelReminderWork()
        }

        Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showOverwriteDialog(
        oldType: String,
        newValue: Int,
        onConfirm: () -> Unit
    ) {
        val oldTypeText = if (oldType == GOAL_TYPE_MANUAL) {
            "manual"
        } else {
            "calculada"
        }

        AlertDialog.Builder(this)
            .setTitle("Substituir meta atual?")
            .setMessage("A meta $oldTypeText atual será substituída por $newValue ml. Deseja continuar?")
            .setPositiveButton("Sim") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun requestPermissionAndScheduleReminder(frequency: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            scheduleReminderWork(frequency)
        }
    }

    private fun scheduleReminderWork(frequency: String) {
        val minutes = when (frequency) {
            "30 min" -> 30L
            "1 hora" -> 60L
            "2 horas" -> 120L
            "3 horas" -> 180L
            else -> 60L
        }

        val workRequest =
            PeriodicWorkRequestBuilder<WaterReminderWorker>(minutes, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WaterReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelReminderWork() {
        WorkManager.getInstance(this)
            .cancelUniqueWork(WaterReminderWorker.WORK_NAME)
    }

    private fun getSelectedFrequency(): String {
        return binding.spinnerFrequency.selectedItem?.toString() ?: "1 hora"
    }

    companion object {
        private const val KEY_DAILY_GOAL_ML = "daily_goal_ml"
        private const val KEY_GOAL_TYPE = "goal_type"
        private const val KEY_MANUAL_GOAL_ML = "manual_goal_ml"
        private const val KEY_CALCULATED_GOAL_ML = "calculated_goal_ml"
        private const val KEY_CALCULATED_WEIGHT = "calculated_weight"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        private const val KEY_REMINDER_FREQUENCY = "reminder_frequency"

        private const val GOAL_TYPE_MANUAL = "manual"
        private const val GOAL_TYPE_CALCULATED = "calculated"
    }
}