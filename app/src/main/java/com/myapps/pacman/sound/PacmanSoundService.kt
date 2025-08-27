package com.myapps.pacman.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.annotation.RawRes
import com.myapps.pacman.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PacmanSoundService(
    context: Context
):GameSoundService{

    companion object {
        private const val MAX_TONE = 0.5f
        private const val MIDDLE_TONE = 0.1f
        private const val LOWER_TONE = 0.05f
        private const val SIREN_TONE = 0.01f

    }

    private lateinit var audioAttributes: AudioAttributes
    private lateinit var soundPool: SoundPool
    private lateinit var soundMap: MutableMap<Int, Int>
    private lateinit var streamMap: MutableMap<Int, Int?>
    private var isSoundMuted = false


    init {
        init(context)
    }

    private fun init(context: Context) {
        audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(4)
            .build()
        soundMap = mutableMapOf()
        streamMap = mutableMapOf()
        loadSounds(context)
    }

    private fun loadSounds(context: Context) {
        soundMap[R.raw.pacman_intro] = soundPool.load(context, R.raw.pacman_intro, 1)
        soundMap[R.raw.ghost_siren] = soundPool.load(context, R.raw.ghost_siren, 1)
        soundMap[R.raw.pacman_energizer_mode] = soundPool.load(context, R.raw.pacman_energizer_mode, 1)
        soundMap[R.raw.pacman_eatghost] = soundPool.load(context, R.raw.pacman_eatghost, 1)
        soundMap[R.raw.pacman_eating_fruit] = soundPool.load(context, R.raw.pacman_eating_fruit, 1)
        soundMap[R.raw.pacman_death] = soundPool.load(context, R.raw.pacman_death, 1)
        soundMap[R.raw.pacman_eating_pellet] = soundPool.load(context, R.raw.pacman_eating_pellet, 1)
        soundMap[R.raw.pacman_extra_life] = soundPool.load(context, R.raw.pacman_extra_life, 1)
        streamMap[R.raw.pacman_intro] = null
        streamMap[R.raw.ghost_siren] = null
        streamMap[R.raw.pacman_energizer_mode] = null
        streamMap[R.raw.pacman_eatghost] = null
        streamMap[R.raw.pacman_eating_fruit] = null
        streamMap[R.raw.pacman_death] = null
        streamMap[R.raw.pacman_eating_pellet] = null
        streamMap[R.raw.pacman_extra_life] = null
    }

    override fun playSound(
        @RawRes
        soundId: Int
    ) {
        val sound = soundMap[soundId]
        val currentStream = streamMap[soundId]
        val soundTone = getSoundTone(soundId)

        if (currentStream != null) {
            stopSound(soundId)
        }
        streamMap[soundId] = sound?.let {
            soundPool.play(
                it,
                if (!isSoundMuted) soundTone else 0f,
                if (!isSoundMuted) soundTone else 0f,
                0,
                0,
                1f
            )
        }

    }

    override fun muteSounds() {
        isSoundMuted = true
        for (i in soundMap.keys) {
            val stream = streamMap[i]
            stream?.let {
                soundPool.setVolume(it, 0f, 0f)
            }
        }
    }

    override fun recoverSound() {
        isSoundMuted = false
        for (i in soundMap.keys) {
            val soundTone = getSoundTone(i)
            val stream = streamMap[i]
            stream?.let {
                soundPool.setVolume(it, soundTone, soundTone)
            }
        }
    }

    override fun pauseSound(
        @RawRes
        soundId: Int
    ) {
        val stream = streamMap[soundId]
        stream?.let {
            soundPool.pause(it)
        }
    }

    override fun stopSound(
        @RawRes
        soundId: Int
    ) {
        val sound = streamMap[soundId]
        sound?.let {
            soundPool.stop(it)
        }
    }

    fun release() {
        soundPool.release()
        soundMap.clear()
        streamMap.clear()
    }

    private fun getSoundTone(soundId: Int): Float {
        return when (soundId) {
            R.raw.pacman_intro,
            R.raw.pacman_extra_life,
            R.raw.pacman_eating_fruit,
            R.raw.pacman_death -> MAX_TONE

            R.raw.pacman_eating_pellet -> LOWER_TONE
            R.raw.pacman_energizer_mode,
            R.raw.pacman_eatghost -> MIDDLE_TONE

            R.raw.ghost_siren -> SIREN_TONE
            else -> SIREN_TONE
        }
    }
}
