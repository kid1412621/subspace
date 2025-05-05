package me.nanova.subspace.domain.model

import me.nanova.subspace.R

enum class AccountType {
    QBITTORENT,
    TRANSMISSION,
//    ARIA2,
    ;

    fun toMonoIcon(): Int = when (this) {
        QBITTORENT -> R.drawable.ic_qb_mono
        TRANSMISSION -> R.drawable.ic_transmission_mono
    }

    fun toIcon(): Int = when (this) {
        QBITTORENT -> R.drawable.ic_qb
        TRANSMISSION -> R.drawable.ic_transmission
    }
}