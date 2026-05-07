package fr.junade.gainznote.i18n

import java.util.Locale

actual fun getSystemLanguage(): String = Locale.getDefault().language
