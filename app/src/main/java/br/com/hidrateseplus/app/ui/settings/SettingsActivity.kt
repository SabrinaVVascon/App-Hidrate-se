package br.com.hidrateseplus.app.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.hidrateseplus.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupExpandableSections()
        setupFrequencySpinner()
        setupReminderSwitch()
        setupButtons()
    }

    private fun setupExpandableSections() {
        binding.headerCalculatedGoal.setOnClickListener {
            toggleSection(
                binding.layoutCalculatedGoal,
                binding.tvToggleCalculated
            )
        }

        binding.headerManualGoal.setOnClickListener {
            toggleSection(
                binding.layoutManualGoal,
                binding.tvToggleManual
            )
        }
    }

    private fun toggleSection(layout: View, toggleText: android.widget.TextView) {
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
            binding.layoutFrequency.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupButtons() {
        binding.btnCalculateGoal.setOnClickListener {
            val weightText = binding.etWeight.text.toString().trim()

            if (weightText.isEmpty()) {
                Toast.makeText(this, "Digite o peso para calcular", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightText.toDoubleOrNull()

            if (weight == null) {
                Toast.makeText(this, "Peso inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calculatedGoal = (weight * 35).toInt()
            binding.tvCalculatedGoal.text = "Meta diária (ml): $calculatedGoal"
        }

        binding.btnSave.setOnClickListener {
            val manualGoal = binding.etGoal.text.toString().trim()
            val remindersEnabled = binding.switchReminder.isChecked
            val frequency = if (remindersEnabled) {
                binding.spinnerFrequency.selectedItem.toString()
            } else {
                "Lembretes desativados"
            }

            Toast.makeText(
                this,
                "Configurações salvas",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}