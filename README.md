# Tetris

### Things I learned during this project:
* Iterating over a parallel stream of a synchronized collection is not automatically thread-safe.
* `Timer.schedule(...)` vs `Timer.scheduleAtFixedRate(...)`.
* To set an event handler on the entire window in TornadoFX, register it on the current stage during the `View`'s `onDock()` method.
* To have fine control of multiple key presses at once, keep track of the keys pressed and released.
* If it's necessary to stop a thread that is currently sleeping, call its `interrupt()` method. Wherever the `Thread.sleep()` inside of that thread is executed must be wrapped in a `try-catch` block that handles `InterruptedException`.