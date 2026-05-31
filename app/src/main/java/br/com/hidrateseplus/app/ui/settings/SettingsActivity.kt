package br.com.hidrateseplus.app.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.com.hidrateseplus.app.databinding.ActivitySettingsBinding
import br.com.hidrateseplus.app.ui.auth.LoginActivity
import br.com.hidrateseplus.app.worker.WaterReminderWorker
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

// ============================================================
// MVVM — View da tela de Configurações
// Responsabilidade: observar estado e delegar ações.
// WorkManager e permissões ficam aqui pois são Android APIs
// que precisam de Context — a ViewModel só decide o que fazer.
// ============================================================
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModels()

    // Evita loop quando a tela está sendo atualizada pelo estado da ViewModel
    private var isRenderingState = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val msg = if (granted) "Lembretes ativados" else "Permissão de notificação negada"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (granted) scheduleReminderWork(getSelectedFrequency())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFrequencySpinner()
        setupExpandableSections()
        setupClickListeners()
        setupObservers()
    }

    // ── Observers ────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.uiState.observe(this) { state ->
            renderState(state)
        }

        viewModel.event.observe(this) { event ->
            when (event) {
                is SettingsEvent.ShowToast ->
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()

                is SettingsEvent.ShowOverwriteDialog ->
                    showOverwriteDialog(event.oldType, event.newValueMl)

                is SettingsEvent.RequestNotificationPermission ->
                    requestPermissionAndScheduleReminder(getSelectedFrequency())

                is SettingsEvent.CancelReminder ->
                    cancelReminderWork()

                is SettingsEvent.SavedAndFinish -> {
                    Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show()
                    finish()
                }

                null -> {
                    // sem evento pendente
                }

                else -> {
                    // evento não tratado
                }
            }

            if (event != null) viewModel.onEventHandled()
        }
    }

    private fun renderState(state: SettingsUiState) {
        isRenderingState = true

        if (binding.etGoal.text.toString() != state.manualGoal) {
            binding.etGoal.setText(state.manualGoal)
        }

        if (binding.etWeight.text.toString() != state.weight) {
            binding.etWeight.setText(state.weight)
        }

        binding.tvCalculatedGoal.text = state.calculatedGoalText
        binding.switchReminder.isChecked = state.remindersEnabled
        binding.layoutFrequency.visibility = if (state.remindersEnabled) View.VISIBLE else View.GONE
        binding.spinnerFrequency.setSelection(state.selectedFrequencyIndex)

        when (state.goalType) {
            SettingsViewModel.GOAL_TYPE_MANUAL -> {
                binding.layoutManualGoal.visibility = View.VISIBLE
                binding.layoutCalculatedGoal.visibility = View.GONE
                binding.tvToggleManual.text = "−"
                binding.tvToggleCalculated.text = "+"
            }

            SettingsViewModel.GOAL_TYPE_CALCULATED -> {
                binding.layoutCalculatedGoal.visibility = View.VISIBLE
                binding.layoutManualGoal.visibility = View.GONE
                binding.tvToggleCalculated.text = "−"
                binding.tvToggleManual.text = "+"
            }

            else -> {
                binding.layoutManualGoal.visibility = View.GONE
                binding.layoutCalculatedGoal.visibility = View.GONE
                binding.tvToggleManual.text = "+"
                binding.tvToggleCalculated.text = "+"
            }
        }

        isRenderingState = false
    }

    // ── Click Listeners ───────────────────────────────────────

    private fun setupClickListeners() {
        binding.btnCalculateGoal.setOnClickListener {
            viewModel.onCalculateGoalClicked(binding.etWeight.text.toString())
        }

        binding.btnSave.setOnClickListener {
            viewModel.onSaveClicked(
                manualGoalText = binding.etGoal.text.toString(),
                weightText = binding.etWeight.text.toString(),
                remindersEnabled = binding.switchReminder.isChecked,
                frequencyIndex = binding.spinnerFrequency.selectedItemPosition
            )
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.etGoal.doAfterTextChanged { editable ->
            if (!isRenderingState) {
                viewModel.onManualGoalChanged(editable?.toString().orEmpty())
            }
        }

        binding.etWeight.doAfterTextChanged { editable ->
            if (!isRenderingState) {
                viewModel.onWeightChanged(editable?.toString().orEmpty())
            }
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutFrequency.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    // ── Logout ────────────────────────────────────────────────

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Encerrar sessão")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sair") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()

        // Cancela lembretes agendados para evitar notificações após sair da conta
        cancelReminderWork()

        Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ── Android APIs: permissão + WorkManager ────────────────

    private fun requestPermissionAndScheduleReminder(frequency: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            scheduleReminderWork(frequency)
        }
    }

    private fun scheduleReminderWork(frequency: String) {
        val minutes = viewModel.getFrequencyMinutes(frequency)

        val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            minutes,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WaterReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelReminderWork() {
        WorkManager.getInstance(this).cancelUniqueWork(WaterReminderWorker.WORK_NAME)
    }

    private fun getSelectedFrequency(): String {
        return binding.spinnerFrequency.selectedItem?.toString() ?: "1 hora"
    }

    private fun showOverwriteDialog(oldType: String, newValueMl: Int) {
        val oldTypeText = if (oldType == SettingsViewModel.GOAL_TYPE_MANUAL) {
            "manual"
        } else {
            "calculada"
        }

        AlertDialog.Builder(this)
            .setTitle("Substituir meta atual?")
            .setMessage("A meta $oldTypeText atual será substituída por $newValueMl ml. Deseja continuar?")
            .setPositiveButton("Sim") { _, _ ->
                viewModel.onOverwriteConfirmed(
                    manualGoalText = binding.etGoal.text.toString(),
                    weightText = binding.etWeight.text.toString(),
                    remindersEnabled = binding.switchReminder.isChecked,
                    frequencyIndex = binding.spinnerFrequency.selectedItemPosition
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Layout helpers ────────────────────────────────────────

    private fun setupExpandableSections() {
        binding.headerCalculatedGoal.setOnClickListener {
            binding.layoutCalculatedGoal.visibility = View.VISIBLE
            binding.layoutManualGoal.visibility = View.GONE
            binding.tvToggleCalculated.text = "−"
            binding.tvToggleManual.text = "+"
        }

        binding.headerManualGoal.setOnClickListener {
            binding.layoutManualGoal.visibility = View.VISIBLE
            binding.layoutCalculatedGoal.visibility = View.GONE
            binding.tvToggleManual.text = "−"
            binding.tvToggleCalculated.text = "+"
        }
    }

    private fun setupFrequencySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("30 min", "1 hora", "2 horas", "3 horas")
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrequency.adapter = adapter
    }
}