package com.und.model.jpa

import com.und.model.jpa.Segment
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "live_segment")
class LiveSegment {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "live_segment_id_seq")
    @SequenceGenerator(name = "live_segment_id_seq", sequenceName = "live_segment_id_seq", allocationSize = 1)
    var id: Long = 0L

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "segment_id")
    @NotNull
    var segmentId: Long = 0L

    @Column(name="live_segment_type",nullable = false)
    lateinit var liveSegmentType:String

    @Column(name = "start_event")
    @NotNull
    var startEvent: String = ""

    @Column(name = "end_event")
    var endEvent: String = ""

    @Column(name = "start_event_filter")
    var startEventFilter: String = "{}"

    @Column(name = "end_event_filter")
    var endEventFilter: String = "{}"

    @Column(name = "end_event_done")
    var endEventDone: Boolean = false

    @Column(name = "interval_seconds")
    @NotNull
    var interval: Long = 0L

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

}