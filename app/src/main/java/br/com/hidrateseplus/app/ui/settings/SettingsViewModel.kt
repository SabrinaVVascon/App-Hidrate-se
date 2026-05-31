package br.com.hidrateseplus.app.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// ============================================================
// MVVM — ViewModel da tela de Configurações
// Responsabilidade: toda lógica de meta, lembretes e cálculo.
// Usa AndroidViewModel para acessar SharedPreferences
// sem depender da Activity.
// ============================================================

// Estado completo da tela de Settings
data class SettingsUiState(
    val manualGoal: String = "",
    val weight: String = "",
    val calculatedGoalText: String = "Meta diária (ml): --",
    val remindersEnabled: Boolean = false,
    val selectedFrequencyIndex: Int = 1,
    val goalType: String? = null
)

// Eventos únicos que a Activity precisa executar
sealed class SettingsEvent {
    object SavedAndFinish : SettingsEvent()
    data class ShowToast(val message: String) : SettingsEvent()
    data class ShowOverwriteDialog(val oldType: String, val newValueMl: Int) : SettingsEvent()
    data class ScheduleReminder(val frequencyMinutes: Long) : SettingsEvent()
    object CancelReminder : SettingsEvent()
    object RequestNotificationPermission : SettingsEvent()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("hydratese_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableLiveData(SettingsUiState())
    val uiState: LiveData<SettingsUiState> = _uiState

    private val _event = MutableLiveData<SettingsEvent?>()
    val event: LiveData<SettingsEvent?> = _event

    // Valor calculado internamente
    private var calculatedGoalMl: Int? = null
    private var selectedGoalType: String? = null

    init {
        loadSavedSettings()
    }

    fun loadSavedSettings() {
        val remindersEnabled = prefs.getBoolean(KEY_REMINDERS_ENABLED, false)
        val savedFrequency = prefs.getString(KEY_REMINDER_FREQUENCY, "1 hora") ?: "1 hora"
        val savedManualGoal = prefs.getInt(KEY_MANUAL_GOAL_ML, 0)
        val savedCalculatedGoal = prefs.getInt(KEY_CALCULATED_GOAL_ML, 0)
        val savedCalculatedWeight = prefs.getString(KEY_CALCULATED_WEIGHT, "") ?: ""
        val savedGoalType = prefs.getString(KEY_GOAL_TYPE, null)

        val frequencies = listOf("30 min", "1 hora", "2 horas", "3 horas")
        val freqIndex = frequencies.indexOf(savedFrequency).coerceAtLeast(0)

        selectedGoalType = savedGoalType

        calculatedGoalMl = if (
            savedGoalType == GOAL_TYPE_CALCULATED &&
            savedCalculatedGoal > 0
        ) {
            savedCalculatedGoal
        } else {
            null
        }

        val manualGoalText = if (
            savedGoalType == GOAL_TYPE_MANUAL &&
            savedManualGoal > 0
        ) {
            savedManualGoal.toString()
        } else {
            ""
        }

        val weightText = if (savedGoalType == GOAL_TYPE_CALCULATED) {
            savedCalculatedWeight
        } else {
            ""
        }

        val calcText = if (
            savedGoalType == GOAL_TYPE_CALCULATED &&
            savedCalculatedGoal > 0
        ) {
            "Meta diária (ml): $savedCalculatedGoal"
        } else {
            "Meta diária (ml): --"
        }

        _uiState.value = SettingsUiState(
            manualGoal = manualGoalText,
            weight = weightText,
            calculatedGoalText = calcText,
            remindersEnabled = remindersEnabled,
            selectedFrequencyIndex = freqIndex,
            goalType = savedGoalType
        )
    }

    fun onManualGoalChanged(manualGoalText: String) {
        selectedGoalType = GOAL_TYPE_MANUAL
        calculatedGoalMl = null

        _uiState.value = _uiState.value?.copy(
            manualGoal = manualGoalText,
            weight = "",
            calculatedGoalText = "Meta diária (ml): --",
            goalType = GOAL_TYPE_MANUAL
        )
    }

    fun onWeightChanged(weightText: String) {
        selectedGoalType = GOAL_TYPE_CALCULATED
        calculatedGoalMl = null

        _uiState.value = _uiState.value?.copy(
            manualGoal = "",
            weight = weightText,
            calculatedGoalText = "Meta diária (ml): --",
            goalType = GOAL_TYPE_CALCULATED
        )
    }

    fun onCalculateGoalClicked(weightText: String) {
        val normalizedWeightText = weightText.trim().replace(",", ".")

        if (normalizedWeightText.isBlank()) {
            _event.value = SettingsEvent.ShowToast("Digite o peso para calcular")
            return
        }

        val weight = normalizedWeightText.toDoubleOrNull()

        if (weight == null || weight <= 0) {
            _event.value = SettingsEvent.ShowToast("Peso inválido")
            return
        }

        calculatedGoalMl = (weight * 35).toInt()
        selectedGoalType = GOAL_TYPE_CALCULATED

        _uiState.value = _uiState.value?.copy(
            manualGoal = "",
            weight = weightText,
            calculatedGoalText = "Meta diária (ml): $calculatedGoalMl",
            goalType = GOAL_TYPE_CALCULATED
        )
    }

    fun onSaveClicked(
        manualGoalText: String,
        weightText: String,
        remindersEnabled: Boolean,
        frequencyIndex: Int
    ) {
        val frequency = listOf("30 min", "1 hora", "2 horas", "3 horas")[frequencyIndex]
        val currentGoalType = prefs.getString(KEY_GOAL_TYPE, null)
        val currentGoalValue = prefs.getInt(KEY_DAILY_GOAL_ML, 0)
        val manualGoal = manualGoalText.trim().toIntOrNull()

        val goalTypeToSave = when (selectedGoalType) {
            GOAL_TYPE_MANUAL -> {
                if (manualGoal == null || manualGoal <= 0) {
                    _event.value = SettingsEvent.ShowToast("Digite uma meta manual válida")
                    return
                }
                GOAL_TYPE_MANUAL
            }

            GOAL_TYPE_CALCULATED -> {
                if (calculatedGoalMl == null || calculatedGoalMl!! <= 0) {
                    _event.value = SettingsEvent.ShowToast("Calcule a meta antes de salvar")
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
            _event.value = SettingsEvent.ShowOverwriteDialog(currentGoalType, goalValueToSave)
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

    fun onOverwriteConfirmed(
        manualGoalText: String,
        weightText: String,
        remindersEnabled: Boolean,
        frequencyIndex: Int
    ) {
        val frequency = listOf("30 min", "1 hora", "2 horas", "3 horas")[frequencyIndex]
        val currentGoalValue = prefs.getInt(KEY_DAILY_GOAL_ML, 0)
        val manualGoal = manualGoalText.trim().toIntOrNull()

        val goalTypeToSave = selectedGoalType ?: prefs.getString(KEY_GOAL_TYPE, null)

        val goalValueToSave = when (goalTypeToSave) {
            GOAL_TYPE_MANUAL -> manualGoal ?: currentGoalValue
            GOAL_TYPE_CALCULATED -> calculatedGoalMl ?: currentGoalValue
            else -> currentGoalValue
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
            _event.value = SettingsEvent.RequestNotificationPermission
        } else {
            _event.value = SettingsEvent.CancelReminder
        }

        _event.value = SettingsEvent.SavedAndFinish
    }

    fun getFrequencyMinutes(frequency: String): Long {
        return when (frequency) {
            "30 min" -> 30L
            "1 hora" -> 60L
            "2 horas" -> 120L
            "3 horas" -> 180L
            else -> 60L
        }
    }

    fun onEventHandled() {
        _event.value = null
    }

    companion object {
        const val KEY_DAILY_GOAL_ML = "daily_goal_ml"
        const val KEY_GOAL_TYPE = "goal_type"
        const val KEY_MANUAL_GOAL_ML = "manual_goal_ml"
        const val KEY_CALCULATED_GOAL_ML = "calculated_goal_ml"
        const val KEY_CALCULATED_WEIGHT = "calculated_weight"
        const val KEY_REMINDERS_ENABLED = "reminders_enabled"
        const val KEY_REMINDER_FREQUENCY = "reminder_frequency"

        const val GOAL_TYPE_MANUAL = "manual"
        const val GOAL_TYPE_CALCULATED = "calculated"
    }
}