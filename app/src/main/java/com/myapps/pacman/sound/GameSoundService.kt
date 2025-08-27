package com.myapps.pacman.sound

import androidx.annotation.RawRes

interface GameSoundService {
    fun playSound(@RawRes soundId:Int)
    fun muteSounds()
    fun recoverSound()
    fun pauseSound(@RawRes soundId: Int)
    fun stopSound(@RawRes soundId: Int)
}
