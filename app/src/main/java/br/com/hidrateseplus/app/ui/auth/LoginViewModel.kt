package br.com.hidrateseplus.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ============================================================
// MVVM — ViewModel da tela de Login
// Responsabilidade: gerenciar o estado e a navegação do login.
// A Activity só observa e reage — não decide nada sozinha.
// ============================================================
class LoginViewModel : ViewModel() {

    // Evento único de navegação — true = navegar para Home
    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean> = _navigateToHome

    fun onEnterClicked() {
        // Aqui futuramente pode ter validação, autenticação, etc.
        _navigateToHome.value = true
    }

    // Chamado após a Activity consumir o evento de navegação
    fun onNavigationHandled() {
        _navigateToHome.value = false
    }
}