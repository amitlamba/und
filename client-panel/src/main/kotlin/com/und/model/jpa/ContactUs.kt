package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "contact_us")
class ContactUs {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "contact_us_id_seq")
    @SequenceGenerator(name = "contact_us_id_seq", sequenceName = "contact_us_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "name")
    var name: String=""

    @Column(name = "email")
    var email: String=""

    @Column(name = "mobile_no")
    var mobileNo: String=""

    @Column(name="message")
    var message: String=""

    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime


}







