package br.com.hidrateseplus.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

// ============================================================
// MVVM — ViewModel da tela de Login
// Responsabilidade: gerenciar o estado e a navegação do login.
// A Activity só observa e reage — não decide nada sozinha.
// ============================================================
class LoginViewModel : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean> = _navigateToHome

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    fun onEnterClicked(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _navigateToHome.value = true
                } else {
                    _loginError.value = getFriendlyErrorMessage(task.exception?.message)
                }
            }
    }

    private fun getFriendlyErrorMessage(errorMessage: String?): String {
        return when {
            errorMessage == null -> {
                "Não foi possível realizar o login. Tente novamente."
            }

            errorMessage.contains("password", ignoreCase = true) -> {
                "Senha incorreta. Verifique e tente novamente."
            }

            errorMessage.contains("no user record", ignoreCase = true) ||
                    errorMessage.contains("user", ignoreCase = true) -> {
                "Usuário não encontrado. Verifique o e-mail ou crie uma conta."
            }

            errorMessage.contains("network", ignoreCase = true) -> {
                "Erro de conexão. Verifique sua internet e tente novamente."
            }

            errorMessage.contains("badly formatted", ignoreCase = true) -> {
                "E-mail inválido. Use o formato nome@dominio.com."
            }

            else -> {
                "E-mail ou senha inválidos."
            }
        }
    }

    fun onNavigationHandled() {
        _navigateToHome.value = false
    }

    fun onLoginErrorShown() {
        _loginError.value = null
    }
}