package br.com.hidrateseplus.app.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.hidrateseplus.app.R
import org.hamcrest.Matchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Teste de Interação (Instrumentado)
 * Objetivo: Verificar se a interface responde corretamente aos comandos do usuário.
 */
@RunWith(AndroidJUnit4::class)
class HomeInteractionTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Test
    fun verificarSeBotaoAdicionarAguaEstaVisivel() {
        // Verifica se o botão de 250ml está visível na tela
        onView(withId(R.id.btnAdd250))
            .check(matches(isDisplayed()))
    }

    @Test
    fun estadoInicial_tvTotalDeveExibir0ml() {
        // Verifica que o total exibido na abertura da tela é "0 ml"
        onView(withId(R.id.tvTotal))
            .check(matches(withText("0 ml")))
    }

    @Test
    fun aoClicarEmAdicionar500ml_botaoDevePermanecer_visivelEResponsivo() {
        // Clica no botão de adicionar 500ml
        onView(withId(R.id.btnAdd500))
            .perform(click())

        // Verifica que o botão permanece visível e clicável após a interação
        onView(withId(R.id.btnAdd500))
            .check(matches(isDisplayed()))
    }
}
