package com.gainznote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gainznote.i18n.S
import com.gainznote.ui.theme.GainzThemeColors
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Composant de sélection verticale (wheel / picker) type iOS.
 * Fix 2a: range commence à 0 (0..59, 0..23)
 * Fix 2b: onValueChange toujours appelé (pas de guard if > 0)
 * Fix 2c: LaunchedEffect(value) pour resynchroniser quand valeur externe change
 * Fix 2d: pointerInput(key) isolé par instance pour éviter vol de gestes entre roues
 */
@Composable
fun VerticalWheelPicker(
    value: Int,
    range: IntRange,           // Fix 2a: doit toujours inclure 0 si voulu (ex: 0..59)
    onValueChange: (Int) -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 30.dp,
    visibleItems: Int = 3,
    format: (Int) -> String = { it.toString().padStart(2, '0') }
) {
    require(visibleItems % 2 == 1) { "visibleItems must be odd" }
    val total = range.last - range.first + 1
    val padding = visibleItems / 2

    val initialIndex = (value - range.first).coerceIn(0, total - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Fix 2c: Observer scroll → notifier valeur sélectionnée
    LaunchedEffect(listState) {
        snapshotFlow {
            // On attend que le scroll soit terminé pour lire la valeur stable
            listState.firstVisibleItemIndex to listState.isScrollInProgress
        }
            .distinctUntilChanged()
            .collect { (idx, scrolling) ->
                if (!scrolling) {
                    // Fix 2b: pas de guard if (value > 0) — on notifie toujours, y compris 0
                    val newValue = (range.first + idx).coerceIn(range.first, range.last)
                    if (newValue != value) onValueChange(newValue)
                }
            }
    }

    // Fix 2c: resync scroll quand la valeur externe change
    LaunchedEffect(value) {
        val target = (value - range.first).coerceIn(0, total - 1)
        if (listState.firstVisibleItemIndex != target) {
            listState.scrollToItem(target)
        }
    }

    Box(
        // Fix 2d: clipToBounds + pointerInput isolé empêche la propagation des gestes
        modifier = modifier
            .height(itemHeight * visibleItems)
            .clipToBounds()
            .pointerInput(range) { /* consomme les events dans cette zone */ },
        contentAlignment = Alignment.Center
    ) {
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
            modifier = Modifier.fillMaxWidth().height(itemHeight * visibleItems)  // Fix 2d: taille explicite
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
 * Sélecteur de durée composé de 2 ou 3 wheel pickers (heures, minutes, secondes).
 * @param totalSeconds durée totale actuelle en secondes
 * @param onChange callback avec la nouvelle durée totale en secondes
 * @param showHours si false, masque la colonne heures
 */
@Composable
fun DurationWheelPicker(
    totalSeconds: Long,
    onChange: (Long) -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier,
    showHours: Boolean = true
) {
    // Fix 2a: coerceIn 0..23 / 0..59 — range correct
    var h by remember { mutableStateOf((totalSeconds / 3600L).toInt().coerceIn(0, 23)) }
    var m by remember { mutableStateOf(((totalSeconds % 3600L) / 60L).toInt().coerceIn(0, 59)) }
    var s by remember { mutableStateOf((totalSeconds % 60L).toInt().coerceIn(0, 59)) }

    // Fix 2c: LaunchedEffect pour resync si totalSeconds change de l'extérieur
    LaunchedEffect(totalSeconds) {
        val currentTotal = h * 3600L + m * 60L + s.toLong()
        if (currentTotal != totalSeconds) {
            h = (totalSeconds / 3600L).toInt().coerceIn(0, 23)
            m = ((totalSeconds % 3600L) / 60L).toInt().coerceIn(0, 59)
            s = (totalSeconds % 60L).toInt().coerceIn(0, 59)
        }
    }

    // Fix 2b: update() appelé sans condition — 0 est une valeur valide
    fun update(nh: Int = h, nm: Int = m, ns: Int = s) {
        h = nh; m = nm; s = ns
        onChange(nh * 3600L + nm * 60L + ns.toLong())
    }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showHours) {
                Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(S.hoursShort, color = c.textMuted, fontSize = 11.sp)
                    VerticalWheelPicker(
                        value = h, range = 0..23,  // Fix 2a
                        onValueChange = { update(nh = it) }, c = c,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            // Fix 2d: chaque Column est dans son propre scope de gestes
            Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(S.minutesShort, color = c.textMuted, fontSize = 11.sp)
                VerticalWheelPicker(
                    value = m, range = 0..59,  // Fix 2a: 0..59 (pas 1..59)
                    onValueChange = { update(nm = it) }, c = c,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(S.secondsShort, color = c.textMuted, fontSize = 11.sp)
                val sIndex = (s / 5).coerceIn(0, 11)
                VerticalWheelPicker(
                    value = sIndex, range = 0..11,  // Fix 2a: commence à 0
                    onValueChange = { update(ns = it * 5) }, c = c,
                    modifier = Modifier.fillMaxWidth(),
                    format = { (it * 5).toString().padStart(2, '0') }
                )
            }
        }
    }
}
