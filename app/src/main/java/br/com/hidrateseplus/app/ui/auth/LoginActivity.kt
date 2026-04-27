package br.com.hidrateseplus.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.com.hidrateseplus.app.databinding.ActivityLoginBinding
import br.com.hidrateseplus.app.ui.home.HomeActivity

// ============================================================
// MVVM — View da tela de Login
// Responsabilidade: observar estado e reagir.
// Zero lógica de negócio ou navegação decidida aqui.
// ============================================================
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.navigateToHome.observe(this) { shouldNavigate ->
            if (shouldNavigate == true) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                viewModel.onNavigationHandled()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEnter.setOnClickListener {
            viewModel.onEnterClicked()
        }
    }
}