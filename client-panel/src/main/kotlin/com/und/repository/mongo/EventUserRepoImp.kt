package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
import java.time.LocalDate

@Repository
class EventUserRepoImp:EventUserRepo {

    @Autowired
    lateinit var mongoTemplate:MongoTemplate

    override fun totalEventUserToday(): Long {
        var date= LocalDate.now()
        var year=date.year
        var month=date.monthValue
        var day=date.dayOfMonth


        print("$year $month $day")
        val format= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        var newfromDate=format.parse("$year-$month-$day 00:00:00")

        var newtoDate=format.parse("$year-$month-$day 23:29:54")

        var query: Query = Query()
        val addCriteria = query.addCriteria(Criteria.where("creationTime").gte(newfromDate).lte(newtoDate))
        return mongoTemplate.count(query, EventUser::class.java)
    }
}