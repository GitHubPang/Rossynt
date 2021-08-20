package org.example.githubpang.rossynt.services

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class LineSeparatorUtilTest internal constructor(private val resultOffset: Int, private val offset: Int, private val text: String, private val oldSeparator: String, private val newSeparator: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{1}@{2}")
        fun data(): Array<Any> {
            return arrayOf(
                arrayOf(0, 0, "\r\n", "\r\n", "\r"),
                arrayOf(1, 2, "\r\n", "\r\n", "\r"),

                arrayOf(0, 0, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(1, 1, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(2, 2, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(3, 3, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(5, 4, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(6, 5, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(7, 6, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(8, 7, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(10, 8, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(12, 9, "abc\ndef\n\ng", "\n", "\r\n"),
                arrayOf(13, 10, "abc\ndef\n\ng", "\n", "\r\n"),

                arrayOf(0, 0, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(1, 1, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(2, 2, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(3, 3, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(4, 5, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(5, 6, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(6, 7, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(7, 8, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(8, 10, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(9, 12, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
                arrayOf(10, 13, "abc\r\ndef\r\n\r\ng", "\r\n", "\r"),
            )
        }
    }

    // ******************************************************************************** //

    @Test
    fun convertOffset() {
        Assert.assertEquals(resultOffset, LineSeparatorUtil.convertOffset(offset, text, oldSeparator, newSeparator))
    }
}
