package me.nanova.subspace.ui

enum class Routes(name: String) {
    Home(name = "home"),
    AccountCreation(name = "account"),
    AccountDetails(name = "account/{accountId}"),
    Settings(name = "settings"),
    Blank(name = "")
}