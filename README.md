# Tetris

### Things I learned during this project:
* Iterating over a parallel stream of a synchronized collection is not automatically thread-safe.
* `Timer.schedule(...)` vs `Timer.scheduleAtFixedRate(...)`.
* To set an event handler on the entire window in TornadoFX, register it on the current stage during the `View`'s `onDock()` method.
* To have fine control of multiple key presses at once, keep track of the keys pressed and released.