# Tetris
[![Build Status](https://travis-ci.org/ndon55555/Tetris.svg?branch=multiplatform)](https://travis-ci.org/ndon55555/Tetris)

## Requirements
* [Java 12](https://jdk.java.net/12/) on your shell path

## Start Playing
### Desktop
1. `git clone https://github.com/ndon55555/Tetris.git`
2. `cd Tetris`
3. `./gradlew :desktop:runGame`

### Browser
1. `git clone https://github.com/ndon55555/Tetris.git`
2. `cd Tetris`
3. `./gradlew :browser:runServer`
4. Go to `localhost:8080` in your browser.

## Things I learned during this project:
* Iterating over a parallel stream of a synchronized collection is not automatically thread-safe.
* `Timer.schedule(...)` vs `Timer.scheduleAtFixedRate(...)`.
* To set an event handler on the entire window in TornadoFX, register it on the current stage during the `View`'s `onDock()` method.
* To have fine control of multiple key presses at once, keep track of the keys pressed and released.
* If it's necessary to stop a thread that is currently sleeping, call its `interrupt()` method. Wherever the `Thread.sleep()` inside of that thread is executed must be wrapped in a `try-catch` block that handles `InterruptedException`.
* To make sure a thread stops when the main program has ended, set it to a daemon thread before execution.
* At the time of this writing, Travis CI doesn't have a text area where you can input multiline environment variables.
  They only provide a text field. To get around this, simply replace new lines with `\n`, then prefix the string with `$'`
  and append `'` (e.g. `$'like\nthis'`).
