package br.com.hidrateseplus.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.hidrateseplus.data.repository.WaterRepository

// ============================================================
// Factory: necessária para passar o Repository para o ViewModel
// sem usar bibliotecas de injeção de dependência (ex: Hilt)
// ============================================================
class HomeViewModelFactory(
    private val repository: WaterRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    }
}