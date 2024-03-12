package me.nanova.subspace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.nanova.subspace.domain.repo.QTRepo
import me.nanova.subspace.ui.Account

@HiltAndroidApp
class App : Application() {
//    lateinit var container: Container
    override fun onCreate() {
        super.onCreate()
//        container = QtContainer(Account("","","", AccountType.QT))
    }
}

interface Container {
    val QTRepo: QTRepo?
    val account: Account?
}

//class DefaultAppContainer(
////    override val repo: Repo?,
//                          override val account: Account?) : AppContainer {
//
//}
//class QtContainer(override val account: Account) : Container {
//
//    private val retrofitService: QtApiService by lazy {
//        HttpClient(account).getRetrofit()
//            .create(QtApiService::class.java)
//    }
//
//    override val repo: Repo by lazy {
//        NetworkRepo(retrofitService)
//    }
//}

