package br.com.hidrateseplus.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.hidrateseplus.data.repository.Result
import br.com.hidrateseplus.data.repository.WaterError
import br.com.hidrateseplus.data.repository.WaterRepository
import br.com.hidrateseplus.util.ValidationUtils
import kotlinx.coroutines.launch

// ============================================================
// PADRÃO: MVVM — ViewModel
// Responsabilidade: lógica de negócio e estado da tela Home.
// A Activity NÃO acessa o banco ou a API diretamente.
// ============================================================

// Estado completo da tela Home (imutável para a UI)
data class HomeUiState(
    val totalMl: Int = 0,
    val goalMl: Int = 2000,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val undoAvailable: Boolean = false
)

class HomeViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    // LiveData que a Activity observa — nunca expõe o MutableLiveData
    private val _uiState = MutableLiveData(HomeUiState())
    val uiState: LiveData<HomeUiState> = _uiState

    // Evento único (não fica "preso" na tela após rotação)
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage

    // Carrega meta salva nas preferências (passada pela Activity)
    fun loadData(savedGoalMl: Int) {
        _uiState.value = _uiState.value?.copy(
            goalMl = if (savedGoalMl > 0) savedGoalMl else 2000,
            isLoading = true
        )

        viewModelScope.launch {
            when (val result = repository.getTodayTotal()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value?.copy(
                        totalMl = result.data,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is Result.Failure -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        errorMessage = translateError(result.error)
                    )
                }
            }
        }
    }

    // Adiciona água — chamado pelos botões na Activity
    fun addWater(amount: Int) {
        if (!ValidationUtils.isWaterAmountValid(amount)) {
            _snackbarMessage.value = when {
                amount <= 0 -> "O valor deve ser maior que 0"
                amount > 5000 -> "O valor não pode ser maior que 5000 ml"
                else -> "Digite um valor válido"
            }
            return
        }

        viewModelScope.launch {
            when (val result = repository.addWater(amount)) {
                is Result.Success -> {
                    // Recarrega total após inserção
                    refreshTotal()
                }
                is Result.Failure -> {
                    _snackbarMessage.value = translateError(result.error)
                }
            }
        }
    }

    // Desfaz último registro
    fun undoLastEntry() {
        viewModelScope.launch {
            when (val result = repository.undoLastEntry()) {
                is Result.Success -> refreshTotal()
                is Result.Failure -> {
                    _snackbarMessage.value = translateError(result.error)
                }
            }
        }
    }

    // Limpa mensagem do snackbar após exibição
    fun onSnackbarShown() {
        _snackbarMessage.value = null
    }

    // Recarrega o total sem alterar outros estados
    private suspend fun refreshTotal() {
        when (val result = repository.getTodayTotal()) {
            is Result.Success -> {
                _uiState.value = _uiState.value?.copy(
                    totalMl = result.data,
                    errorMessage = null
                )
            }
            is Result.Failure -> {
                _snackbarMessage.value = translateError(result.error)
            }
        }
    }

    // ============================================================
    // Tradução de erros técnicos → mensagens amigáveis para o usuário
    // (requisito do trabalho: tradução de erro para UX)
    // ============================================================
    private fun translateError(error: WaterError): String {
        return when (error) {
            is WaterError.NetworkError -> "Sem conexão com a internet. Dado salvo localmente."
            is WaterError.ServerError -> "Servidor indisponível. Tente novamente mais tarde."
            is WaterError.Unknown -> "Ocorreu um erro inesperado."
        }
    }
}