package fr.junade.gainznote.model

import fr.junade.gainznote.i18n.S

object UnitConverter {
    private const val KG_TO_LBS = 2.20462

    fun displayWeight(kg: Double?, useImperial: Boolean): String {
        val value = kg ?: return ""
        return if (useImperial) {
            val lbs = value * KG_TO_LBS
            if (lbs == lbs.toLong().toDouble()) lbs.toLong().toString() else String.format("%.1f", lbs).replace(",", ".")
        } else {
            if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
        }
    }

    fun displayWeightWithUnit(kg: Double?, useImperial: Boolean): String {
        val value = displayWeight(kg, useImperial)
        if (value.isEmpty()) return "—"
        return "$value ${weightUnit(useImperial)}"
    }

    fun parseWeight(text: String, useImperial: Boolean): Double? {
        val clean = text.replace(',', '.').trim()
        val value = clean.toDoubleOrNull() ?: return null
        return if (useImperial) value / KG_TO_LBS else value
    }

    fun weightUnit(useImperial: Boolean): String = if (useImperial) S.unitLbs else S.unitKg
}
