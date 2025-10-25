package ru.finpact.crypto

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LEN = 16

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val enc = Base64.getEncoder()
        return "\$pbkdf2-sha256\$$ITERATIONS\$${enc.encodeToString(salt)}\$${enc.encodeToString(hash)}"
    }

    fun verify(password: String, stored: String): Boolean =
        try {
            // "$pbkdf2-sha256$<iter>$<salt_b64>$<hash_b64>"
            val parts = stored.split('$').filter { it.isNotEmpty() }
            require(parts.size == 4) { "bad phc format" }
            require(parts[0] == "pbkdf2-sha256") { "unsupported algo: ${parts[0]}" }

            val iterations = parts[1].toInt()
            val salt = Base64.getDecoder().decode(parts[2])
            val expected = Base64.getDecoder().decode(parts[3])
            val actual = pbkdf2(password.toCharArray(), salt, iterations, expected.size * 8)

            constantTimeEquals(expected, actual)
        } catch (_: Throwable) {
            false
        }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int, keyLenBits: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, keyLenBits)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var r = 0
        for (i in a.indices) r = r or (a[i].toInt() xor b[i].toInt())
        return r == 0
    }
}

