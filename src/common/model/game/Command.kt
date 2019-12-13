package model.game

/**
 * Possible player commands.
 */
enum class Command {
    ROTATE_CCW,
    ROTATE_CW,
    LEFT,
    RIGHT,
    SOFT_DROP,
    HARD_DROP,
    HOLD,
    DO_NOTHING
}