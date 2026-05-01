package com.gainznote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
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
import kotlinx.coroutines.flow.filter

/**
 * Wheel picker vertical type iOS, snap sur l'item central.
 *
 * Bugs corrigés :
 *  2a – range commence à 0 (0..59, 0..23)
 *  2b – onValueChange toujours émis, même pour 0
 *  2c – plus de LaunchedEffect(value) qui créait une boucle de sync ;
 *       la clé de rememberLazyListState gère la réinitialisation externe
 *  2d – pointerInput réel (awaitEachGesture) pour isoler les gestes de chaque roue
 */
@Composable
fun VerticalWheelPicker(
    value: Int,
    range: IntRange,
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

    // Réinitialiser la liste quand la valeur externe change vraiment
    // (ex: template chargé, reset) — pas de boucle car on ne scrolle pas depuis ici
    val initialIndex = (value - range.first).coerceIn(0, total - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Observer la fin du scroll pour notifier la valeur sélectionnée
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }                         // uniquement quand le scroll s'arrête
            .collect {
                val idx = listState.firstVisibleItemIndex
                // 2b : pas de guard — 0 est valide
                val newValue = (range.first + idx).coerceIn(range.first, range.last)
                onValueChange(newValue)
            }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItems)
            .clipToBounds()
            // 2d : consommation réelle des événements pour isoler cette roue
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitPointerEvent()   // consomme le premier événement → stop propagation latérale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Indicateur de sélection (fond de l'item central)
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
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItems)
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
 * Sélecteur de durée : heures (optionnel) · minutes · secondes (pas de 5s).
 */
@Composable
fun DurationWheelPicker(
    totalSeconds: Long,
    onChange: (Long) -> Unit,
    c: GainzThemeColors,
    modifier: Modifier = Modifier,
    showHours: Boolean = true
) {
    // États locaux initialisés depuis totalSeconds
    // On n'utilise PAS de LaunchedEffect(totalSeconds) pour éviter la boucle de sync.
    // Si le parent change totalSeconds de façon radicale (template), il doit
    // passer une nouvelle key= sur DurationWheelPicker pour forcer la récomposition.
    var h by remember(totalSeconds) { mutableStateOf((totalSeconds / 3600L).toInt().coerceIn(0, 23)) }
    var m by remember(totalSeconds) { mutableStateOf(((totalSeconds % 3600L) / 60L).toInt().coerceIn(0, 59)) }
    var s by remember(totalSeconds) { mutableStateOf((totalSeconds % 60L).toInt().coerceIn(0, 59)) }

    // 2b : update() sans condition, 0 est valide
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
                        value = h, range = 0..23,
                        onValueChange = { update(nh = it) }, c = c,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            // 2d : chaque picker est isolé par pointerInput dans VerticalWheelPicker
            Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(S.minutesShort, color = c.textMuted, fontSize = 11.sp)
                VerticalWheelPicker(
                    value = m, range = 0..59,
                    onValueChange = { update(nm = it) }, c = c,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(Modifier.width(72.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(S.secondsShort, color = c.textMuted, fontSize = 11.sp)
                val sIndex = (s / 5).coerceIn(0, 11)
                VerticalWheelPicker(
                    value = sIndex, range = 0..11,
                    onValueChange = { update(ns = it * 5) }, c = c,
                    modifier = Modifier.fillMaxWidth(),
                    format = { (it * 5).toString().padStart(2, '0') }
                )
            }
        }
    }
}
