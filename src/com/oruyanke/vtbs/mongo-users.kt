package com.oruyanke.vtbs

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import java.time.LocalDate

data class User(
    @BsonId val uid: String,
    val verified: Boolean,
    val isRoot: Boolean,
    val adminVtubers: List<String>,
    val userSecurity: ObjectId,
    val userProfile: ObjectId
)

data class UserSecurity(
    val salt: String,
    val hash: String,
    val _id: ObjectId? = null
)

data class UserProfile(
    val name: String,
    val email: String,
    val _id: ObjectId? = null
)

data class UserSession(
    val uid: String,
    val activatedDate: LocalDate,
    val _id: ObjectId? = null
)

fun CoroutineClient.userDB() = this.getDatabase("user")
fun CoroutineDatabase.users() = this.getCollection<User>("user_meta")
fun CoroutineDatabase.sessions() = this.getCollection<UserSession>("user_session")
fun CoroutineDatabase.profiles() = this.getCollection<UserProfile>("user_profile")
fun CoroutineDatabase.securities() = this.getCollection<UserSecurity>("user_security")

suspend fun CoroutineClient.userSecurityFor(uid: String): UserSecurity {
    val db = userDB()
    val user = db.users().findOneById(uid)
        ?: throw UserNotFoundException(uid, "meta-data not found")
    return db.securities().findOneById(user.userSecurity)
        ?: throw UserNotFoundException(uid, "security data not found")
}

suspend fun CoroutineClient.userProfileFor(uid: String): UserProfile {
    val db = userDB()
    val user = db.users().findOneById(uid)
        ?: throw UserNotFoundException(uid, "meta-data not found")
    return db.profiles().findOneById(user.userProfile)
        ?: throw UserNotFoundException(uid, "profile data not found")
}

suspend fun CoroutineCollection<UserSession>.newSession(uid: String): UserSession {
    deleteMany(UserSession::uid eq uid)
    val session = UserSession(uid, LocalDate.now())
    insertOne(session)
    return session
}

suspend fun CoroutineCollection<User>.hasUser(uid: String) =
    findOneById(uid) != null

suspend fun CoroutineCollection<User>.newUser(user: User) = insertOne(user)

suspend fun CoroutineCollection<User>.byUID(uid: String) =
    findOneById(uid) ?: throw UserNotFoundException(uid, "?")

suspend fun CoroutineCollection<UserSession>.getSession(sessionId: String): UserSession {
    val session = findOneById(ObjectId(sessionId)) ?: throw SessionNotFoundException()
    if (session.isExpired()) {
        throw SessionExpiredException()
    }
    return session
}

suspend fun CoroutineCollection<User>.bySession(session: UserSession) =
    findOneById(session.uid) ?: throw InvalidSessionException()

suspend fun CoroutineCollection<UserSecurity>.newSecurity(salt: String, hash: String): ObjectId {
    val security = UserSecurity(salt = salt, hash = hash)
    insertOne(security)
    return security._id!!
}

suspend fun CoroutineCollection<UserProfile>.newProfile(name: String, email: String): ObjectId {
    val profile = UserProfile(name = name, email = email)
    insertOne(profile)
    return profile._id!!
}
