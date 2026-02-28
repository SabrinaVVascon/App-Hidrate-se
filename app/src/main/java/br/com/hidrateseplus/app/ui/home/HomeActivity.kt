package br.com.hidrateseplus.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.hidrateseplus.app.databinding.ActivityMainBinding
import br.com.hidrateseplus.app.ui.history.HistoryActivity
import br.com.hidrateseplus.app.ui.settings.SettingsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Valores “em memória” (sem persistência)
    private var totalMl = 750
    private var goalMl = 2000

    // Guarda o último valor adicionado pra desfazer corretamente
    private var lastAddedMl = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Atualiza a tela com os valores iniciais
        updateUI()

        // Navegação
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Atalhos
        binding.btnAdd250.setOnClickListener {
            addWater(250)
        }

        binding.btnAdd500.setOnClickListener {
            addWater(500)
        }

        // Adicionar valor manual
        binding.btnAddCustom.setOnClickListener {
            val value = binding.etCustomMl.text.toString().trim().toIntOrNull()

            if (value != null && value > 0) {
                addWater(value)
                binding.etCustomMl.text?.clear()
            } else {
                binding.etCustomMl.error = "Digite um valor válido"
            }
        }

        // Desfazer (remove o último valor adicionado)
        binding.btnUndo.setOnClickListener {
            if (lastAddedMl > 0) {
                totalMl = (totalMl - lastAddedMl).coerceAtLeast(0)
                lastAddedMl = 0
                updateUI()
            }
        }
    }

    private fun addWater(ml: Int) {
        lastAddedMl = ml
        totalMl += ml
        updateUI()
    }

    private fun updateUI() {
        binding.tvTotal.text = "${totalMl} ml"
        binding.progressGoal.max = goalMl
        binding.progressGoal.progress = totalMl.coerceAtMost(goalMl)
        binding.tvGoal.text = "Meta diária: ${goalMl} ml"
    }
}