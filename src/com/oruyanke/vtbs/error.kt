package com.oruyanke.vtbs

import io.ktor.http.HttpStatusCode

open class ResponseException(val code: HttpStatusCode, msg: String) : java.lang.RuntimeException(msg)

class BadRequestException(msg: String) : ResponseException(HttpStatusCode.BadRequest, msg)

open class LoginException(uid: String, reason: String) : ResponseException(
    HttpStatusCode.Forbidden,
    "Login failed for $uid: $reason"
)

open class RegisterException(uid: String, reason: String) : ResponseException(
    HttpStatusCode.Forbidden,
    "Register failed for $uid: $reason"
)

class UserNotFoundException(uid: String, reason: String) : LoginException(
    uid,
    "User '$uid' not found: $reason"
)

class PasswordMismatchException(uid: String) : LoginException(
    uid,
    "Wrong password or account"
)

class UserExistException(uid: String) : RegisterException(
    uid,
    "UserId '$uid' already exists"
)

open class AuthRequiredException(msg: String = "Please login first") : ResponseException(
    HttpStatusCode.Forbidden,
    msg
)

class SessionNotFoundException : AuthRequiredException("Session not found")

class SessionExpiredException : AuthRequiredException("Session expired")

class InvalidSessionException : AuthRequiredException("Invalid session")

open class PermissionDenied(msg: String = "Permission denied") : ResponseException(
    HttpStatusCode.Forbidden,
    msg
)

class RootRequiredException : PermissionDenied(
    "You are not root :)"
)
