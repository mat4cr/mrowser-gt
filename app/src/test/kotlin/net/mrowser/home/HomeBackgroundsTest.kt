package net.mrowser.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeBackgroundsTest {

    @Test fun `pick is deterministic for a given seed`() {
        assertEquals(HomeBackgrounds.pick(42L), HomeBackgrounds.pick(42L))
    }

    @Test fun `pick always returns a known gradient`() {
        (0L until 100L).forEach { assertTrue(HomeBackgrounds.pick(it) in HomeBackgrounds.ALL) }
    }

    @Test fun `pick survives negative seeds`() {
        assertTrue(HomeBackgrounds.pick(-7L) in HomeBackgrounds.ALL)
        assertTrue(HomeBackgrounds.pick(Long.MIN_VALUE) in HomeBackgrounds.ALL)
    }

    @Test fun `consecutive seeds walk the whole list`() {
        val seen = (0L until HomeBackgrounds.ALL.size.toLong()).map { HomeBackgrounds.pick(it) }.toSet()
        assertEquals(HomeBackgrounds.ALL.size, seen.size)
    }
}
