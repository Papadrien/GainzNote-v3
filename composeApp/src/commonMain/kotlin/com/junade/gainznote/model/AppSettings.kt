package com.junade.gainznote.model

data class AppSettings(
    val darkTheme: Boolean = true,
    val chronoNotifEnabled: Boolean = false,
    val adFree: Boolean = false,
    val language: String = "auto",
    val lastWorkoutType: WorkoutType = WorkoutType.MUSCULATION
)
