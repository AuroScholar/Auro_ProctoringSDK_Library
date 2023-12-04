package com.example.auroproctoringsdk.languageSetup

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class CurrentLanguage {

    fun getCurrentLocalizationLanguageName(context: Context): String {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0).displayName
        } else {
            configuration.locale.displayName
        }
    }
    fun getCurrentLocalizationLanguageCode(context: Context): String {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0).language
        } else {
            configuration.locale.language
        }
    }
    fun setLocale(languageToLoad: String,context: Context) {
        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
