package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * CREATE TABLE campign_trigger
(
id             BIGSERIAL NOT NULL CONSTRAINT campaign_trigger_pkey PRIMARY KEY,
client_id      BIGINT    NOT NULL REFERENCES appuser,
campaign_id    BIGINT    NOT NULL REFERENCES campaign,
trigger_time   TIMESTAMP NOT NULL,
trigger_status SMALLINT  NOT NULL
);
 */

@Entity
@Table(name = "campaign_trigger")
class CampaignTrigger {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_trigger_id_seq")
    @SequenceGenerator(name = "campaign_trigger_id_seq", sequenceName = "campaign_trigger_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "campaign_id")
    @NotNull
    var campaignID: Long? = null

    @Column(name = "trigger_time")
    @NotNull
    var triggerTime: LocalDate? = null

    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime
}