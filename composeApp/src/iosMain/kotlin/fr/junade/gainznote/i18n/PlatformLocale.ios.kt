package fr.junade.gainznote.i18n

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.countryCode

actual fun getSystemLanguage(): String =
    NSLocale.currentLocale.languageCode ?: "en"

actual fun getSystemCountry(): String =
    NSLocale.currentLocale.countryCode ?: ""
