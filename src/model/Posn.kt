package model

/**
 * Represents a position on a Cartesian plane.
 *
 * @property x The x-coordinate.
 * @property y The y-coordinate.
 */
data class Posn(val x: Double, val y: Double) {
    /**
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

    /**
     * @param dX The amount the x-coordinate changes.
     * @param dY The amount the y-coordinate changes.
     *
     * Moves this model.Posn over the given amounts.
     */
    fun translate(dX: Double, dY: Double): Posn = Posn(x + dX, y + dY)

    /**
     * @param centerX Center x-coordinate of rotation.
     * @param centerY Center y-coordinate of rotation.
     *
     * Rotates this model.Posn 90 degrees clockwise around the given central coordinates.
     */
    fun rotate90CWAround(centerX: Double, centerY: Double): Posn {
        val dX = x - centerX
        val dY = y - centerY

        return Posn((dY + centerX), (-dX + centerY))
    }

    /**
     * @param centerOfRotation The model.Posn to rotate around.
     *
     * Rotates this model.Posn 90 degrees clockwise around the given model.Posn.
     */
    fun rotate90CWAround(centerOfRotation: Posn): Posn = rotate90CWAround(centerOfRotation.x, centerOfRotation.y)

    /**
     * @param centerX Center x-coordinate of rotation.
     * @param centerY Center y-coordinate of rotation.
     *
     * Rotates this model.Posn 90 degrees counter-clockwise around the given central coordinates.
     */
    fun rotate90CCWAround(centerX: Double, centerY: Double): Posn {
        val dX = x - centerX
        val dY = y - centerY

        return Posn((-dY + centerX), (dX + centerY))
    }

    /**
     * @param centerOfRotation The model.Posn to rotate around.
     *
     * Rotates this model.Posn 90 degrees counter-clockwise around the given model.Posn.
     */
    fun rotate90CCWAround(centerOfRotation: Posn): Posn = rotate90CCWAround(centerOfRotation.x, centerOfRotation.y)
}