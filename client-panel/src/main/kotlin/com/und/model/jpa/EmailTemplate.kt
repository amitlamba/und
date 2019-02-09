package com.und.model.jpa

import com.und.model.EditorSelected
import com.und.model.MessageType
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
//import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@Table(name = "email_template")
class EmailTemplate {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "email_template_id_seq")
    @SequenceGenerator(name = "email_template_id_seq", sequenceName = "email_template_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "appuser_id")
    @NotNull
    var appuserID: Long? = null

    @Column(name = "name")
    @NotNull
    lateinit var name: String

    @OneToOne(fetch = FetchType.LAZY )
    @JoinColumn(name="email_template_body")
    @Cascade(CascadeType.ALL)
    var emailTemplateBody: Template? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="email_template_subject")
    @Cascade(CascadeType.ALL)
    var emailTemplateSubject: Template? = null

    @Column(name = "parent_id")
    @Null
    var parentID: Long? = null

//    @Column(name = "from_user")
//    @NotNull
//    lateinit var from: String

    @Column(name = "message_type") //Promotional or Transactional
    @NotNull
    @Enumerated(EnumType.STRING)
    var messageType: MessageType? = null

    @Column(name = "tags")
    var tags: String? = null

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

    @Enumerated(EnumType.STRING)
    @Column(name="editor_selected")
    var editorSelected: EditorSelected?=null

//    @Column(name = "status")
//    @NotNull
//    @Enumerated(EnumType.STRING)
//    lateinit var status: Status
}

