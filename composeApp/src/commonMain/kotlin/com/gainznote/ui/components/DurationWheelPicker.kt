package com.gainznote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainznote.i18n.S
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Composant de sélection verticale (wheel / picker) type iOS.
 * - La valeur affichée au centre est la valeur sélectionnée.
 * - Défilement fluide avec snap.
 * - Listes finies : on préremplit avec des éléments "vides" au début et à la fin
 *   pour que la 1re et la dernière valeur puissent être centrées.
 */
@Composable
fun VerticalWheelPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 40.dp,
    visibleItems: Int = 5,
    format: (Int) -> String = { it.toString().padStart(2, '0') }
) {
    require(visibleItems % 2 == 1) { "visibleItems must be odd" }
    val total = range.last - range.first + 1
    val padding = visibleItems / 2
    val paddedSize = total + 2 * padding

    // index dans la LazyColumn = value - range.first + padding
    val initialIndex = (value - range.first).coerceIn(0, total - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Observer le premier item visible : c'est l'index de la valeur centrée.
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { idx ->
                val newValue = range.first + idx.coerceIn(0, total - 1)
                if (newValue != value) onValueChange(newValue)
            }
    }

    // Si la valeur externe change, scroller
    LaunchedEffect(value) {
        val target = (value - range.first).coerceIn(0, total - 1)
        if (listState.firstVisibleItemIndex != target) {
            listState.scrollToItem(target)
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleItems), contentAlignment = Alignment.Center) {
        // Indicateur de sélection (barres top/bottom autour de l'item central)
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(c.accentDim, RoundedCornerShape(8.dp))
        )
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(vertical = itemHeight * padding),
            modifier = Modifier.fillMaxSize()
        ) {
            items(total) { idx ->
                val v = range.first + idx
                val isSelected = v == value
                Box(
                    Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        format(v),
                        color = if (isSelected) c.accent else c.textSec,
                        fontSize = if (isSelected) 22.sp else 17.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/**
 * Sélecteur de durée composé de 3 wheel pickers (heures, minutes, secondes).
 * Version corrigée avec gestion d'état indépendante et layout optimisé.
 * 
 * @param totalSeconds durée totale actuelle en secondes
 * @param onChange callback avec la nouvelle durée totale en secondes
 * @param showHours si false, masque la colonne heures (utile pour les durées courtes)
 * 
 * Corrections apportées:
 * - Gestion d'état indépendante pour chaque roue
 * - Réduction des espaces verticaux (padding réduit)
 * - Conservation de la zone centrale de sélection
 * - Prévention de la réinitialisation lors des changements
 */
@Composable
fun DurationWheelPicker(
    totalSeconds: Long,
    onChange: (Long) -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier,
    showHours: Boolean = true
) {
    // États locaux pour maintenir les valeurs indépendamment
    var currentHours by remember(totalSeconds) { 
        mutableIntStateOf((totalSeconds / 3600L).toInt().coerceIn(0, 23)) 
    }
    var currentMinutes by remember(totalSeconds) { 
        mutableIntStateOf(((totalSeconds % 3600L) / 60L).toInt().coerceIn(0, 59)) 
    }
    var currentSeconds by remember(totalSeconds) { 
        mutableIntStateOf((totalSeconds % 60L).toInt().coerceIn(0, 59)) 
    }

    // Fonction pour émettre les changements sans réinitialiser les autres valeurs
    fun emitChange(newHours: Int? = null, newMinutes: Int? = null, newSecs: Int? = null) {
        val h = newHours ?: currentHours
        val m = newMinutes ?: currentMinutes
        val s = newSecs ?: currentSeconds

        // Mettre à jour les états locaux
        if (newHours != null) currentHours = h
        if (newMinutes != null) currentMinutes = m
        if (newSecs != null) currentSeconds = s

        // Émettre le changement total
        onChange(h * 3600L + m * 60L + s.toLong())
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Augmenté pour plus de clarté
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showHours) {
            Column(
                Modifier.weight(1f), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    S.hoursShort, 
                    color = c.textMuted, 
                    fontSize = 12.sp, // Légèrement augmenté pour la lisibilité
                    modifier = Modifier.padding(bottom = 4.dp) // Réduction du padding
                )
                VerticalWheelPicker(
                    value = currentHours, 
                    range = 0..23,
                    onValueChange = { emitChange(newHours = it) }, 
                    c = c,
                    modifier = Modifier.fillMaxWidth(),
                    itemHeight = 36.dp, // Hauteur réduite pour moins d'espace
                    visibleItems = 3 // Réduit à 3 items visibles pour compacter
                )
            }
        }

        Column(
            Modifier.weight(1f), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                S.minutesShort, 
                color = c.textMuted, 
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            VerticalWheelPicker(
                value = currentMinutes, 
                range = 0..59,
                onValueChange = { emitChange(newMinutes = it) }, 
                c = c,
                modifier = Modifier.fillMaxWidth(),
                itemHeight = 36.dp,
                visibleItems = 3
            )
        }

        Column(
            Modifier.weight(1f), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                S.secondsShort, 
                color = c.textMuted, 
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            VerticalWheelPicker(
                value = currentSeconds, 
                range = 0..59,
                onValueChange = { emitChange(newSecs = it) }, 
                c = c,
                modifier = Modifier.fillMaxWidth(),
                itemHeight = 36.dp,
                visibleItems = 3
            )
        }
    }
}