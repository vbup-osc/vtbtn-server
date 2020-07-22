package com.oruyanke.vtbs

import io.ktor.application.Application
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.upsert
import randomSalt
import toPasswordHash
import withSalt

@ExperimentalUnsignedTypes
suspend fun Application.installEnvironment() {
    val envRoot: String? = System.getenv(ServerConfig.ENV_ROOT_NAME)
    val envPassword: String? = System.getenv(ServerConfig.ENV_ROOT_PASSWORD)

    if (envRoot.isNullOrBlank() || envPassword.isNullOrBlank()) {
        return
    }

    val mongo: CoroutineClient by inject()
    val db = mongo.userDB()

    val salt = randomSalt()
    val securityId = db.securities().newSecurity(
        salt = salt,
        hash = envPassword.withSalt(salt).toPasswordHash()
    )

    val profileId = db.profiles().newProfile(
        name = envRoot,
        email = "$envRoot@islovely.icu"
    )

    val user = User(
        uid = envRoot,
        verified = true,
        isRoot = true,
        adminVtubers = listOf(),
        userSecurity = securityId,
        userProfile = profileId
    )

    db.users().updateOneById(
        envRoot,
        user,
        upsert()
    )
}
