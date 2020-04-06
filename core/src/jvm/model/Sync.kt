package model

actual fun <R> sync(lock: Any, f: () -> R): R = synchronized(lock) { f() }