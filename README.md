# Tetris
[![Build Status](https://travis-ci.org/ndon55555/Tetris.svg?branch=master)](https://travis-ci.org/ndon55555/Tetris)
[![Maintainability](https://api.codeclimate.com/v1/badges/facb6670bb0b5d530150/maintainability)](https://codeclimate.com/github/ndon55555/Tetris/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/facb6670bb0b5d530150/test_coverage)](https://codeclimate.com/github/ndon55555/Tetris/test_coverage)

### Requirements
* [Java 12](https://jdk.java.net/12/) on your shell path

### Start Playing
1. `git clone https://github.com/ndon55555/Tetris.git`
2. `cd Tetris`
3. `./gradlew run`

### Things I learned during this project:
* Iterating over a parallel stream of a synchronized collection is not automatically thread-safe.
* `Timer.schedule(...)` vs `Timer.scheduleAtFixedRate(...)`.
* To set an event handler on the entire window in TornadoFX, register it on the current stage during the `View`'s `onDock()` method.
* To have fine control of multiple key presses at once, keep track of the keys pressed and released.
* If it's necessary to stop a thread that is currently sleeping, call its `interrupt()` method. Wherever the `Thread.sleep()` inside of that thread is executed must be wrapped in a `try-catch` block that handles `InterruptedException`.
* To make sure a thread stops when the main program has ended, set it to a daemon thread before execution.
