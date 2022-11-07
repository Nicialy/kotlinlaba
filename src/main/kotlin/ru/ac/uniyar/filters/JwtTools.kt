package ru.ac.uniyar.filters

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

private const val ACCEPT_LiFE_IN_SECONDS = 60L

private const val DEFAULT_TOKEN_LIVE = 7

class JwtTools(
    secret: String,
    private val issuer: String,
    private val expiresIn: Period = Period.ofDays(DEFAULT_TOKEN_LIVE)
) {
    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier by lazy {
        JWT
            .require(algorithm)
            .acceptExpiresAt(ACCEPT_LiFE_IN_SECONDS)
            .build()
    }
    fun create(subject: String): String? {
        return try {
            JWT
                .create()
                .withSubject(subject)
                .withIssuer(issuer)
                .withExpiresAt(
                    Date.from(
                        LocalDate
                            .now()
                            .plus(expiresIn)
                            .atStartOfDay()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    )
                )
                .sign(algorithm)
        } catch (_: JWTCreationException) {
            null
        }
    }
    fun subject(token: String): String? {
        return try {
            val decodedToken = verifier.verify(token)
            decodedToken.subject
        } catch (_: JWTVerificationException) {
            null
        }
    }
}
