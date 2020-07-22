import io.ktor.utils.io.core.toByteArray
import java.security.MessageDigest
import java.util.*

fun String.withSalt(salt: String) = this + salt

fun randomSalt() = UUID.randomUUID().toString()

@ExperimentalUnsignedTypes
fun String.toPasswordHash() =
    MessageDigest.getInstance("sha-256")
        .digest(this.toByteArray())
        .toHexString()

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() =
    asUByteArray().joinToString("") {
        it.toString(16).padStart(2, '0')
    }
