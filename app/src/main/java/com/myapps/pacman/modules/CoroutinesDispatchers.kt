package com.myapps.pacman.modules

import com.myapps.pacman.modules.qualifiers.DispatcherDefault
import com.myapps.pacman.modules.qualifiers.DispatcherIO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesDispatchers {
    @Provides
    @DispatcherIO
    fun providesDispatchersIO(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DispatcherDefault
    fun providesDispatchersDefault(): CoroutineDispatcher = Dispatchers.Default
}
