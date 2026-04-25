package com.gainznote.model

data class AppSettings(
    val darkTheme: Boolean = true,
    val blackBg: Boolean = false,
    val chronoNotifEnabled: Boolean = false,
    val adFree: Boolean = false,
    val language: String = "auto",
    val lastWorkoutType: WorkoutType = WorkoutType.MUSCULATION
)
