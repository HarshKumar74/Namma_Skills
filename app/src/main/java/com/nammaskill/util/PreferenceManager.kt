package com.nammaskill.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("namma_skill_prefs", Context.MODE_PRIVATE)

    fun saveFavoriteTrade(trade: String) {
        val favorites = getFavoriteTrades().toMutableSet()
        favorites.add(trade)
        prefs.edit().putStringSet("favorite_trades", favorites).apply()
    }

    fun getFavoriteTrades(): Set<String> {
        return prefs.getStringSet("favorite_trades", emptySet()) ?: emptySet()
    }
}
