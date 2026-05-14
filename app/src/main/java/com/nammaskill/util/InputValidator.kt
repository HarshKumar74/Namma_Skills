package com.nammaskill.util

object InputValidator {
    fun isValidName(name: String): Boolean {
        return name.isNotBlank()
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
    }
}

//annotation class InputValidator
