package me.nanova.subspace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.data.NetworkRepo
import me.nanova.subspace.data.Repo
import me.nanova.subspace.domain.QtApiService
import me.nanova.subspace.ui.Account
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
//    lateinit var container: Container
    override fun onCreate() {
        super.onCreate()
//        container = QtContainer(Account("","","", AccountType.QT))
    }
}

interface Container {
    val repo: Repo?
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

