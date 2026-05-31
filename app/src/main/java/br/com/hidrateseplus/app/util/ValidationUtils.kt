package br.com.hidrateseplus.app.util

import android.util.Patterns

object ValidationUtils {

    fun isFieldNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }

    fun isEmailValid(email: String): Boolean {
        return email.trim().isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.trim().length >= 6
    }

    fun isWaterAmountValid(value: String): Boolean {
        val amount = value.trim().toIntOrNull()
        return amount != null && amount > 0
    }

    fun isWaterAmountValid(amount: Int): Boolean {
        return amount > 0
    }
}