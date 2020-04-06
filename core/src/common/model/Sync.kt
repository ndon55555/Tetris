package model

expect fun <R> sync(lock: Any, f: () -> R): R