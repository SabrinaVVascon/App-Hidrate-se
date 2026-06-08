package br.com.hidrateseplus.app.util

import org.junit.Assert.*
import org.junit.Test

/**
 * Teste Unitário para ValidationUtils
 * Responsabilidade: Validar as regras de negócio de entrada de dados.
 */
class ValidationUtilsTest {

    @Test
    fun `isFieldNotEmpty deve retornar true quando o campo nao esta vazio`() {
        val result = ValidationUtils.isFieldNotEmpty("Água")
        assertTrue("O campo deveria ser considerado não vazio", result)
    }

    @Test
    fun `isFieldNotEmpty deve retornar false quando o campo esta em branco`() {
        val result = ValidationUtils.isFieldNotEmpty("   ")
        assertFalse("O campo deveria ser considerado vazio", result)
    }

    @Test
    fun `isWaterAmountValid deve retornar true para valores positivos`() {
        val result = ValidationUtils.isWaterAmountValid("250")
        assertTrue("O valor 250 deveria ser válido", result)
    }

    @Test
    fun `isWaterAmountValid deve retornar false para zero ou negativos`() {
        assertFalse("O valor 0 deveria ser inválido", ValidationUtils.isWaterAmountValid("0"))
        assertFalse("O valor -100 deveria ser inválido", ValidationUtils.isWaterAmountValid("-100"))
    }

    @Test
    fun `isWaterAmountValid deve retornar false para texto nao numerico`() {
        val result = ValidationUtils.isWaterAmountValid("abc")
        assertFalse("Texto não numérico deveria ser inválido", result)
    }

    @Test
    fun `isPasswordValid deve retornar true para 6 ou mais caracteres`() {
        assertTrue(ValidationUtils.isPasswordValid("123456"))
        assertTrue(ValidationUtils.isPasswordValid("senha_forte"))
    }

    @Test
    fun `isPasswordValid deve retornar false para menos de 6 caracteres`() {
        assertFalse(ValidationUtils.isPasswordValid("12345"))
    }

    @Test
    fun `isFieldNotEmpty deve retornar false quando o campo esta completamente vazio`() {
        val result = ValidationUtils.isFieldNotEmpty("")
        assertFalse("String vazia deveria ser considerada vazia", result)
    }

    @Test
    fun `isWaterAmountValid com Int deve retornar true para valor positivo`() {
        val result = ValidationUtils.isWaterAmountValid(500)
        assertTrue("O valor inteiro 500 deveria ser válido", result)
    }

    @Test
    fun `isWaterAmountValid com Int deve retornar false para zero ou negativo`() {
        assertFalse("O valor inteiro 0 deveria ser inválido", ValidationUtils.isWaterAmountValid(0))
        assertFalse("O valor inteiro -1 deveria ser inválido", ValidationUtils.isWaterAmountValid(-1))
    }
}
