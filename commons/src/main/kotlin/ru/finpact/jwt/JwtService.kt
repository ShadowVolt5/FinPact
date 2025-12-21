package ru.finpact.jwt

import kotlinx.serialization.json.*
import ru.finpact.contracts.core.ContractViolation
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object JwtService {

    private val jsonCodec: Json = Json { encodeDefaults = false }

    private val signingKeyBytes: ByteArray by lazy {
        val secretEnv = System.getenv("JWT_SECRET")?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("JWT_SECRET env is required")
        secretEnv.toByteArray(Charsets.UTF_8)
    }
    private val tokenIssuer: String by lazy { System.getenv("JWT_ISSUER") ?: "finpact-auth" }
    private val tokenAudience: String? by lazy { System.getenv("JWT_AUDIENCE")?.ifBlank { null } }
    private val tokenTtlSeconds: Long by lazy { System.getenv("JWT_TTL_SECONDS")?.toLongOrNull() ?: 3600L }

    fun generateJwtToken(userId: Long, email: String): String {
        val issuedAtSeconds = Instant.now().epochSecond
        val expiresAtSeconds = issuedAtSeconds + tokenTtlSeconds

        val headerJson = JsonObject(
            mapOf(
                "alg" to JsonPrimitive("HS256"),
                "typ" to JsonPrimitive("JWT")
            )
        )

        val payloadJson = buildPayload(
            userId = userId,
            email = email,
            issuer = tokenIssuer,
            audience = tokenAudience,
            issuedAt = issuedAtSeconds,
            expiresAt = expiresAtSeconds
        )

        val headerBase64 = base64UrlEncode(jsonCodec.encodeToString(JsonObject.serializer(), headerJson).toByteArray())
        val payloadBase64 = base64UrlEncode(jsonCodec.encodeToString(JsonObject.serializer(), payloadJson).toByteArray())
        val signatureBase64 = signToBase64Url("$headerBase64.$payloadBase64")

        return "$headerBase64.$payloadBase64.$signatureBase64"
    }

    fun verifyAndDecodeClaims(jwtToken: String): Map<String, Any?> {
        val parts = jwtToken.split('.')
        if (parts.size != 3) throw ContractViolation.unauthorized("invalid token")

        val headerBase64 = parts[0]
        val payloadBase64 = parts[1]
        val signatureBase64 = parts[2]

        val expectedSignatureBytes = hmacSha256("$headerBase64.$payloadBase64")
        val actualSignatureBytes = base64UrlDecodeToBytes(signatureBase64)
        if (!constantTimeEqualsBytes(expectedSignatureBytes, actualSignatureBytes)) {
            throw ContractViolation.unauthorized("invalid token")
        }

        val payloadJsonString = base64UrlDecodeToString(payloadBase64)
        val payload = try {
            jsonCodec.parseToJsonElement(payloadJsonString).jsonObject
        } catch (_: Throwable) {
            throw ContractViolation.unauthorized("invalid token")
        }

        val now = Instant.now().epochSecond
        val exp = payload["exp"]?.jsonPrimitive?.longOrNull ?: throw ContractViolation.unauthorized("invalid token")
        if (now >= exp) throw ContractViolation.unauthorized("token expired")

        val iss = payload["iss"]?.jsonPrimitive?.content ?: throw ContractViolation.unauthorized("invalid token")
        if (iss != tokenIssuer) throw ContractViolation.unauthorized("invalid token")

        tokenAudience?.let { expectedAud ->
            val aud = payload["aud"]?.jsonPrimitive?.content ?: throw ContractViolation.unauthorized("invalid token")
            if (aud != expectedAud) throw ContractViolation.unauthorized("invalid token")
        }

        val sub = payload["sub"]?.jsonPrimitive?.content
        val email = payload["email"]?.jsonPrimitive?.content
        val iat = payload["iat"]?.jsonPrimitive?.longOrNull

        return mapOf(
            "sub" to sub,
            "email" to email,
            "iat" to iat,
            "exp" to exp
        )
    }

    fun extractUserIdFromToken(jwtToken: String): Long =
        verifyAndDecodeClaims(jwtToken)["sub"]?.toString()?.toLongOrNull()
            ?: throw ContractViolation.unauthorized("invalid token")

    fun extractEmailFromToken(jwtToken: String): String =
        verifyAndDecodeClaims(jwtToken)["email"]?.toString()
            ?: throw ContractViolation.unauthorized("invalid token")

    private fun buildPayload(
        userId: Long,
        email: String,
        issuer: String,
        audience: String?,
        issuedAt: Long,
        expiresAt: Long
    ): JsonObject {
        val map = mutableMapOf<String, JsonPrimitive>()
        map["iss"] = JsonPrimitive(issuer)
        if (audience != null) map["aud"] = JsonPrimitive(audience)
        map["sub"] = JsonPrimitive(userId.toString())
        map["email"] = JsonPrimitive(email)
        map["iat"] = JsonPrimitive(issuedAt)
        map["exp"] = JsonPrimitive(expiresAt)
        return JsonObject(map)
    }

    private fun signToBase64Url(data: String): String =
        base64UrlEncode(hmacSha256(data))

    private fun hmacSha256(data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(signingKeyBytes, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun base64UrlEncode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private fun base64UrlDecodeToBytes(base64Url: String): ByteArray =
        Base64.getUrlDecoder().decode(base64Url)

    private fun base64UrlDecodeToString(base64Url: String): String =
        String(base64UrlDecodeToBytes(base64Url), Charsets.UTF_8)

    private fun constantTimeEqualsBytes(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].toInt() xor b[i].toInt())
        return diff == 0
    }
}
