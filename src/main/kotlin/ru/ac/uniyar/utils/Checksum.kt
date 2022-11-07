package ru.ac.uniyar.utils

import java.security.MessageDigest

private const val MASK = 0xFF

class Checksum(private val data: ByteArray) {
    private val algorithm = "SHA-512"

    private fun generateChecksum(): String {
        val digest = MessageDigest.getInstance(algorithm)
        return printableHexString(digest.digest(data))
    }

    private fun printableHexString(digestedHash: ByteArray): String {
        return digestedHash.map {
            Integer.toHexString(MASK and it.toInt())
        }
            .map {
                if (it.length < 2) "0$it" else it
            }
            .fold("") { acc, d -> acc + d }
    }

    override fun toString(): String {
        return generateChecksum()
    }
}
