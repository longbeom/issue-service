package com.longbeom.userservice.service

import com.longbeom.userservice.config.JWTProperties
import com.longbeom.userservice.domain.entity.User
import com.longbeom.userservice.domain.repository.UserRepository
import com.longbeom.userservice.exception.PasswordNotMatchedException
import com.longbeom.userservice.exception.UserExistsException
import com.longbeom.userservice.exception.UserNotFoundException
import com.longbeom.userservice.model.SignInRequest
import com.longbeom.userservice.model.SignInResponse
import com.longbeom.userservice.model.SignUpRequest
import com.longbeom.userservice.utils.BCryptUtils
import com.longbeom.userservice.utils.JWTClaim
import com.longbeom.userservice.utils.JWTUtils
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtProperties: JWTProperties,
    private val cacheManager: CoroutineCacheManager<User>,
) {

    companion object {
        private val CACHE_TTL = Duration.ofMinutes(1)
    }

    suspend fun signUp(signUpRequest: SignUpRequest) {
        with(signUpRequest) {
            userRepository.findByEmail(email)?.let {
                throw UserExistsException()
            }
            val user = User(
                email = email,
                password = BCryptUtils.hash(password),
                username = username,
            )
            userRepository.save(user)
        }
    }

    suspend fun signIn(request: SignInRequest): SignInResponse {
        return with(userRepository.findByEmail(request.email) ?: throw UserNotFoundException()) {
            val verified = BCryptUtils.verify(request.password, password)
            if (!verified) {
                throw PasswordNotMatchedException()
            }
            val jwtClaim = JWTClaim(
                userId = id!!,
                email = email,
                profileUrl = profileUrl,
                username = username
            )

            val token = JWTUtils.createToken(
                claim = jwtClaim,
                properties = jwtProperties,
            )

            cacheManager.awaitPut(key = token, value = this, ttl = CACHE_TTL)

            SignInResponse(
                email = email,
                username = username,
                token = token,
            )
        }
    }
}