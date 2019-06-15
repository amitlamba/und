package com.und.model.jpa

import com.und.web.model.DidEvents
import com.und.web.model.Geography
import com.und.web.model.GlobalFilter
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "segment")
class Segment  {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "segment_id_seq")
    @SequenceGenerator(name = "segment_id_seq", sequenceName = "segment_id_seq", allocationSize = 1)
    open var id: Long? = null


    @Column(name = "name")
    @NotNull
    open var name: String = ""

    @Column(name = "client_id")
    @NotNull
    open var clientID: Long? = null


    @Column(name = "appuser_id")
    @NotNull
    open var appuserID: Long? = null



    @Column(name = "type")
    @NotNull
    var type: String = ""

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

    @Column(name = "conversion_event")
    @NotNull
    var conversionEvent: String = ""

    @Column(name = "data")
    @NotNull
    @Basic(fetch = FetchType.LAZY)
    var data: String = "{}"

    //add live segment filed in web model

}

class SegmentData {
    var conversionEvent: String = ""
    var didEvents: DidEvents = DidEvents()
    var didNotEvents: DidEvents = DidEvents()
    var globalFilters: List<GlobalFilter> = listOf()
    var geographyFilters: List<Geography> = listOf()
}