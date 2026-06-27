package fr.junade.gainznote.i18n

/** Retourne le code langue système (ex: "fr", "en"). */
expect fun getSystemLanguage(): String

/** Retourne le code pays système (ex: "FR", "US"). */
expect fun getSystemCountry(): String

/** Retourne true si le pays utilise le système impérial. */
fun isImperialCountry(country: String): Boolean = country in listOf("US", "LR", "MM")
