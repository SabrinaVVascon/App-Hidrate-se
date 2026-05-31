package br.com.hidrateseplus.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.com.hidrateseplus.app.databinding.ActivityLoginBinding
import br.com.hidrateseplus.app.ui.home.HomeActivity
import br.com.hidrateseplus.app.util.ValidationUtils

// ============================================================
// MVVM — View da tela de Login
// Responsabilidade: observar estado e reagir.
// Validação de formato dos campos fica aqui; regras de negócio no ViewModel.
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

        viewModel.loginError.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.onLoginErrorShown()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEnter.setOnClickListener {
            if (!validateLoginFields()) return@setOnClickListener

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.onEnterClicked(email, password)
        }

        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateLoginFields(): Boolean {
        binding.etEmail.error = null
        binding.etPassword.error = null

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        var isValid = true

        if (!ValidationUtils.isFieldNotEmpty(email)) {
            binding.etEmail.error = "Informe seu email"
            isValid = false
        } else if (!ValidationUtils.isEmailValid(email)) {
            binding.etEmail.error = "Email inválido. Use o formato nome@dominio.com"
            isValid = false
        }

        if (!ValidationUtils.isFieldNotEmpty(password)) {
            binding.etPassword.error = "Informe sua senha"
            isValid = false
        } else if (!ValidationUtils.isPasswordValid(password)) {
            binding.etPassword.error = "A senha deve ter pelo menos 6 caracteres"
            isValid = false
        }

        return isValid
    }
}