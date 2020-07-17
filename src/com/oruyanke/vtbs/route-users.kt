package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.toHttpDate
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.date.GMTDate
import io.ktor.util.date.Month
import io.ktor.utils.io.core.toByteArray
import org.koin.ktor.ext.inject
import org.litote.kmongo.addEachToSet
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.eq
import org.litote.kmongo.pullAll
import org.litote.kmongo.updateOne
import java.security.MessageDigest
import java.time.LocalDate
import java.util.*

@ExperimentalUnsignedTypes
fun Route.userRoutes() {
    route("/users") {
        val mongo: CoroutineClient by inject()

        get("/hi") {
            errorAware {
                val user = sessionUser(mongo)
                call.respond(
                    mapOf(
                        "msg" to "Hello! ${user.uid}"
                    )
                )
            }
        }

        post<LoginRequest>("/login") {
            errorAware {
                val security = mongo.userSecurityFor(it.uid)
                val hash = it.password.withSalt(security.salt).toPasswordHash()
                if (hash != security.hash) {
                    throw PasswordMismatchException(it.uid)
                }

                val session = mongo.userDB().sessions().newSession(it.uid)
                val expire = session.expireDate().toHttpDate()
                val cookie = "${UserConfig.SESSION_ID}=${session._id!!}; Path=/; Expires=$expire"
                call.response.header("Set-Cookie", cookie)
                call.respond(HttpStatusCode.OK)
            }
        }

        post<RegisterRequest>("/register") {
            errorAware {
                val db = mongo.userDB()
                if (db.users().hasUser(it.uid)) {
                    throw UserExistException(it.uid)
                }

                val salt = randomSalt()
                val securityId = db.securities().newSecurity(
                    salt = salt,
                    hash = it.password.withSalt(salt).toPasswordHash()
                )

                val profileId = db.profiles().newProfile(
                    name = it.name,
                    email = it.email
                )

                val user = User(
                    uid = it.uid,
                    verified = false,
                    isRoot = false,
                    adminVtubers = listOf(),
                    userProfile = profileId,
                    userSecurity = securityId
                )

                mongo.userDB().users().newUser(user)
                call.respond(HttpStatusCode.OK)
            }
        }

        post<ChangeAdminVtuberRequest>("/change-admin-vtuber") {
            errorAware {
                sessionUser(mongo).mustBeRoot()

                mongo.userDB().users().bulkWrite(
                    updateOne(
                        User::uid eq it.uid,
                        addEachToSet(User::adminVtubers, it.add ?: listOf())
                    ),
                    updateOne(
                        User::uid eq it.uid,
                        pullAll(User::adminVtubers, it.remove ?: listOf())
                    )
                )

                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private data class LoginRequest(
    val uid: String,
    val password: String
)

private data class RegisterRequest(
    val uid: String,
    val password: String,
    val name: String,
    val email: String
)

private data class ChangeAdminVtuberRequest(
    val uid: String,
    val add: List<String>?,
    val remove: List<String>?
)

private fun String.withSalt(salt: String) = this + salt

private fun randomSalt() = UUID.randomUUID().toString()

@ExperimentalUnsignedTypes
private fun String.toPasswordHash() =
    MessageDigest.getInstance("sha-256")
        .digest(this.toByteArray())
        .toHexString()

@ExperimentalUnsignedTypes
private fun ByteArray.toHexString() =
    asUByteArray().joinToString("") {
        it.toString(16).padStart(2, '0')
    }

private fun LocalDate.toHttpDate() =
    GMTDate(0, 0, 0, dayOfMonth, Month.from(monthValue), year)
        .toHttpDate()

private fun User.mustBeRoot() {
    if (!this.isRoot) throw RootRequiredException()
}
