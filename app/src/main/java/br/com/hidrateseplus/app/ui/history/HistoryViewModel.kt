package br.com.hidrateseplus.app.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.hidrateseplus.data.local.HistoryDay
import br.com.hidrateseplus.data.repository.Result
import br.com.hidrateseplus.data.repository.WaterRepository
import kotlinx.coroutines.launch

// ============================================================
// MVVM — ViewModel da tela de Histórico
// Responsabilidade: buscar e expor os dados de histórico.
// Substitui os dados hardcoded que estavam na Activity.
// ============================================================
class HistoryViewModel(
    private val repository: WaterRepository
) : ViewModel() {

    private val _historyItems = MutableLiveData<List<HistoryDay>>()
    val historyItems: LiveData<List<HistoryDay>> = _historyItems

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getHistory()) {
                is Result.Success -> {
                    _historyItems.value = result.data
                    _errorMessage.value = null
                }
                is Result.Failure -> {
                    _errorMessage.value = "Não foi possível carregar o histórico."
                }
            }
            _isLoading.value = false
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }
}