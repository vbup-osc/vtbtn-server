package com.oruyanke.vtbs

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class Category(val name: String, val groups: List<Group>)

data class Group(val name: String, val voices: List<Voice>)

data class Voice(val url: String, val text: List<LocalizedText>)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(LocalizedText.Zh::class, name = "zh"),
    JsonSubTypes.Type(LocalizedText.En::class, name = "en"),
    JsonSubTypes.Type(LocalizedText.Jp::class, name = "jp")
)
sealed class LocalizedText {
    abstract val text: String

    class Zh(override val text: String) : LocalizedText()
    class En(override val text: String) : LocalizedText()
    class Jp(override val text: String) : LocalizedText()
}
