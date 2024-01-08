package me.nanova.subspace

import android.app.Application
import me.nanova.subspace.domain.AppContainer
import me.nanova.subspace.domain.DefaultAppContainer

class App : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}