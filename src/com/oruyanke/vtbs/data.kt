package com.oruyanke.vtbs

data class Vtuber(val name: String, val voiceGroups: List<VoiceGroup>)

data class VoiceGroup(val name: String, val text: List<LocalizedText>, val voices: List<Voice>)

data class Voice(val url: String, val text: List<LocalizedText>)

data class LocalizedText(val lang: String, val text: String) {
    companion object {
        @JvmStatic
        fun zh(text: String) = LocalizedText("zh", text)

        @JvmStatic
        fun en(text: String) = LocalizedText("en", text)

        @JvmStatic
        fun jp(text: String) = LocalizedText("jp", text)
    }
}

fun List<LocalizedText>.toMap(): Map<String, String> =
    this.map { Pair(it.lang, it.text) }
        .toMap()
