package net.mrowser.data

/** App-wide settings. Defaults are the shipped values. Immutable — update via copy(). */
data class Settings(
    val autoOpenPlayer: Boolean = true,
    val cursorSpeed: CursorSpeed = CursorSpeed.NORMAL,
    /** Internal bookkeeping, not a user preference: default favorites written once. */
    val seeded: Boolean = false,
    /** Internal bookkeeping, not a user preference: hold-BACK hint shown once. */
    val navHintShown: Boolean = false
)
