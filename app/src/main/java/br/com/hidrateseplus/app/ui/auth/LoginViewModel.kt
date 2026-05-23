package br.com.hidrateseplus.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.com.hidrateseplus.data.remote.AuthRemoteDataSource
import br.com.hidrateseplus.data.repository.UserRepository

// ============================================================
// MVVM — ViewModel da tela de Login
// Responsabilidade: gerenciar o estado e a navegação do login.
// A Activity só observa e reage — não decide nada sozinha.
// ============================================================
class LoginViewModel : ViewModel() {

    private val userRepository = UserRepository(AuthRemoteDataSource())

    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean> = _navigateToHome

    private val _loginError = MutableLiveData<String?>()
    val loginError: LiveData<String?> = _loginError

    fun onEnterClicked(email: String, password: String) {
        // Por enquanto mantém navegação direta; descomente o bloco abaixo quando a API estiver pronta:
        // viewModelScope.launch {
        //     when (val result = userRepository.login(email, password)) {
        //         is Result.Success -> _navigateToHome.value = true
        //         is Result.Error -> _loginError.value = result.message
        //         is Result.Loading -> Unit
        //     }
        // }
        _navigateToHome.value = true
    }

    fun onNavigationHandled() {
        _navigateToHome.value = false
    }

    fun onLoginErrorShown() {
        _loginError.value = null
    }
}
