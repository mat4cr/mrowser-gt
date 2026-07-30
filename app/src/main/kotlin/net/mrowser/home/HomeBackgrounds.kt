package net.mrowser.home

/** Pure: the home overlay's random background gradients. No Android types. */
object HomeBackgrounds {

    /** A two-stop vertical gradient, top to bottom, as ARGB ints. */
    data class Gradient(val top: Int, val bottom: Int)

    /** All kept low-luminance so white text and the #E50914 accent stay legible. */
    val ALL: List<Gradient> = listOf(
        Gradient(0xFF0B0B0F.toInt(), 0xFF1A0E12.toInt()), // ember
        Gradient(0xFF080F14.toInt(), 0xFF0E1A22.toInt()), // deep sea
        Gradient(0xFF100A18.toInt(), 0xFF1C1024.toInt()), // violet
        Gradient(0xFF0A0F0C.toInt(), 0xFF12201A.toInt()), // pine
        Gradient(0xFF120C0A.toInt(), 0xFF231310.toInt()), // rust
        Gradient(0xFF0C0C0C.toInt(), 0xFF1B1B20.toInt())  // graphite
    )

    /** Deterministic for a given [seed]; safe for negative seeds. */
    fun pick(seed: Long): Gradient = ALL[(((seed % ALL.size) + ALL.size) % ALL.size).toInt()]
}
