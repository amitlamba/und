package com.und.model.utils

import java.io.Serializable
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


class EmailTemplate : Serializable{

    var id: Long? = null

    @NotNull
    @Size(min = 2, max = 50)
    @Pattern(regexp = "[A-za-z0-9-_][A-za-z0-9-_\\s]+")
    lateinit var name: String

    @NotNull
    lateinit var emailTemplateBody: String

    @NotNull
    @Size(min = 1, max = 128)
    lateinit var emailTemplateSubject: String

    var parentID: Long? = null

//    @NotNull
//    @Email
//    lateinit var from: String

    @NotNull
    var messageType: MessageType? = null

    var tags: String? = null

    @NotNull
    var editorSelected: EditorSelected? = null
}

enum class EditorSelected {
    tinymceEditor,
    textareaEditor,
}

