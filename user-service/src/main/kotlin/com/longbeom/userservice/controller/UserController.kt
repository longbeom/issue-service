package com.longbeom.userservice.controller

import com.longbeom.userservice.model.SignInRequest
import com.longbeom.userservice.model.SignInResponse
import com.longbeom.userservice.model.SignUpRequest
import com.longbeom.userservice.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/users")
@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping("/signup")
    suspend fun signUp(@RequestBody request: SignUpRequest) =
        userService.signUp(request)

    @PostMapping("/signin")
    suspend fun signIn(@RequestBody request: SignInRequest) : SignInResponse =
        userService.signIn(request)
}