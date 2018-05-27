package com.und.model.redis

import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Id
import javax.validation.constraints.NotNull

@RedisHash("template")
class CachedTemplate {


    @Id
    lateinit var id: String

    @Column(name = "template")
    @NotNull
    lateinit var template: String



    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

}

