package com.myapps.pacman

sealed interface PacmanEvents {
    data object Start: PacmanEvents
    data object Stop: PacmanEvents
    data object Pause: PacmanEvents
    data object Resume: PacmanEvents
    data object RightDirection: PacmanEvents
    data object LeftDirection: PacmanEvents
    data object UpDirection: PacmanEvents
    data object DownDirection: PacmanEvents
    data object MuteSounds: PacmanEvents
    data object RecoverSounds: PacmanEvents

    // Quiz events
    data object ShowQuiz: PacmanEvents
    data object ContinueToNextLevel: PacmanEvents
    data object FailedQuiz: PacmanEvents
}