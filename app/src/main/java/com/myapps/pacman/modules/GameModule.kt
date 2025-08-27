package com.myapps.pacman.modules

import android.content.Context
import com.myapps.pacman.game.CollisionHandler
import com.myapps.pacman.game.ICollisionHandler
import com.myapps.pacman.game.PacmanGame
import com.myapps.pacman.game.coroutines.CoroutineSupervisor
import com.myapps.pacman.levels.JsonMapProvider
import com.myapps.pacman.levels.MapProvider
import com.myapps.pacman.modules.qualifiers.DispatcherDefault
import com.myapps.pacman.sound.GameSoundService
import com.myapps.pacman.sound.PacmanSoundService
import com.myapps.pacman.timer.CentralTimerController
import com.myapps.pacman.timer.ICentralTimerController
import com.myapps.pacman.timer.Timer
import com.myapps.pacman.timer.TimerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {
    @Provides
    @Singleton
    fun providesGameSoundService(@ApplicationContext context: Context):GameSoundService = PacmanSoundService(context)

    @Provides
    @Singleton
    fun providesTimer(@DispatcherDefault coroutineDispatcher: CoroutineDispatcher):TimerInterface = Timer(coroutineDispatcher)

    @Provides
    @Singleton
    fun providesMapProvider(@ApplicationContext context: Context):MapProvider = JsonMapProvider(context)

    @Provides
    @Singleton
    fun providesCoroutineSupervisor(@DispatcherDefault coroutineDispatcher: CoroutineDispatcher): CoroutineSupervisor = CoroutineSupervisor(coroutineDispatcher)


    @Provides
    @Singleton
    fun providesCollisionHandler(@DispatcherDefault coroutineDispatcher: CoroutineDispatcher): ICollisionHandler =
        CollisionHandler(coroutineDispatcher)


    @Provides
    @Singleton
    fun providesCentralTimerController(timerInterface: TimerInterface): ICentralTimerController =
        CentralTimerController(timerInterface)

    @Provides
    @Singleton
    fun providesPacmanGame(
        centralTimerController: ICentralTimerController,
        gameSoundService: GameSoundService,
        mapProvider: MapProvider,
        collisionHandler: ICollisionHandler,
        coroutineSupervisor: CoroutineSupervisor
    ):PacmanGame = PacmanGame(
        centralTimerController,
        gameSoundService,
        mapProvider,
        collisionHandler,
        coroutineSupervisor
    )
}
