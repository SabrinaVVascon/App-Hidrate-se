package br.com.hidrateseplus.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.hidrateseplus.app.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnCreateAccount.setOnClickListener {
            createAccount()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun createAccount() {
        val name = binding.etRegisterName.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirmPassword = binding.etRegisterConfirmPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.etRegisterName.error = "Informe seu nome"
            binding.etRegisterName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etRegisterEmail.error = "Informe seu e-mail"
            binding.etRegisterEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etRegisterPassword.error = "Informe sua senha"
            binding.etRegisterPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etRegisterPassword.error = "A senha deve ter pelo menos 6 caracteres"
            binding.etRegisterPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.etRegisterConfirmPassword.error = "Confirme sua senha"
            binding.etRegisterConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etRegisterConfirmPassword.error = "As senhas não coincidem"
            binding.etRegisterConfirmPassword.requestFocus()
            return
        }

        binding.btnCreateAccount.isEnabled = false
        binding.btnCreateAccount.text = "Criando conta..."

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserName(name)
                } else {
            binding.btnCreateAccount.isEnabled = true
            binding.btnCreateAccount.text = "Criar conta"

            val errorCode = (task.exception as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
            val errorMessage = when (errorCode) {
                "ERROR_INVALID_EMAIL"        -> "O endereço de e-mail está mal formatado."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está em uso."
                "ERROR_WEAK_PASSWORD"        -> "A senha deve ter pelo menos 6 caracteres."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Erro de conexão. Verifique sua internet."
                else -> "Não foi possível criar a conta. Tente novamente."
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
            }
    }

    private fun saveUserName(name: String) {
        val user = firebaseAuth.currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener {
                Toast.makeText(
                    this,
                    "Conta criada com sucesso! Faça login para continuar.",
                    Toast.LENGTH_LONG
                ).show()

                firebaseAuth.signOut()
                finish()
            }
    }
}