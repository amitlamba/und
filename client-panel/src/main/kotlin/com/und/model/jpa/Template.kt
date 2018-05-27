package com.und.model.jpa

//import javax.validation.constraints.Email
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "template")
class Template {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "template_id_seq")
    @SequenceGenerator(name = "template_id_seq", sequenceName = "template_id_seq", allocationSize = 1)
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

    @Column(name = "template")
    @NotNull
    lateinit var template: String


    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

}

