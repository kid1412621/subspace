package me.nanova.subspace.domain.model

import me.nanova.subspace.R

enum class AccountType {
    QT,
    TRANSMISSION,
//    ARIA2,
    ;

    fun toMonoIcon(): Int = when (this) {
        QT -> R.drawable.ic_qt_mono
        TRANSMISSION -> R.drawable.ic_transmission_mono
    }

    fun toIcon(): Int = when (this) {
        QT -> R.drawable.ic_qt
        TRANSMISSION -> R.drawable.ic_transmission
    }
}