package com.und.model

enum class IncludeUsers {
    KNOWN,UNKNOWN,ALL
}

const val USER_DOC = "userDoc"
data class UpdateIdentity(var find:String ="",var update:String="",var clientId:Int=-1)