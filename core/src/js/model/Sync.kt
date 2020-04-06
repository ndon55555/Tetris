package model

actual fun <R> sync(lock: Any, f: () -> R): R = f() // Javascript is single-threaded, so no need to lock on an object