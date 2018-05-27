package com.und.web.model

import com.und.model.EditorSelected
import com.und.model.MessageType
import javax.validation.constraints.NotNull


class EmailTemplate {

    var id: Long? = null

    lateinit var name: String

    @NotNull
    lateinit var emailTemplateBody: String

    @NotNull
    lateinit var emailTemplateSubject: String

    var parentID: Long? = null

    @NotNull
    lateinit var from: String


    @NotNull
    var messageType: MessageType? = null

    var tags: String? = null

    var editorSelected: EditorSelected? = null
}

