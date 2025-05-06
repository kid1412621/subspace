package me.nanova.subspace.util

const val MINIMAL_SUPPORTED_QB_VERSION = "3.2.0"
const val API_CHANGED_QB_VERSION = "5.2.0"

fun isVersionAtLeast(current: String, target: String): Boolean {
    val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
    val targetParts = target.split(".").map { it.toIntOrNull() ?: 0 }
    val maxLength = maxOf(currentParts.size, targetParts.size)
    val paddedCurrent = currentParts + List(maxLength - currentParts.size) { 0 }
    val paddedTarget = targetParts + List(maxLength - targetParts.size) { 0 }
    return paddedCurrent.zip(paddedTarget).firstOrNull { it.first != it.second }?.let {
        it.first > it.second
    } ?: true
}

fun isVersionAtMost(current: String, target: String): Boolean {
    return !isVersionAtLeast(current, target)
}
