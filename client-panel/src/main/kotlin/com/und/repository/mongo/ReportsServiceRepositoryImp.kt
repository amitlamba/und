package com.und.repository.mongo

import com.mongodb.client.model.Aggregates
import com.und.web.model.EventCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository
import java.util.*
import java.time.ZoneId
import java.text.SimpleDateFormat




@Repository
class ReportsServiceRepositoryImp : ReportsServiceRepository {
    @Autowired
    lateinit private var mongoTemplate: MongoTemplate

    override fun getEventsOfUserByDate(clientId: Long, timeZone: TimeZone, fromDate: String, toDate: String): List<EventCount> {


//        val date = input.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        var rawOffSet = timeZone.rawOffset

        println("in repo\n\n\n"+fromDate)
        println("in repo\n\n\n"+toDate)
        val format=SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        var newfromDate=format.parse(fromDate+" 00:00:00")

        var newtoDate=format.parse(toDate+" 23:29:54")
        println("in repo\n\n\n"+newfromDate)
        println("in repo\n\n\n"+newtoDate)
        val project1 = Aggregation.project("name").and("creationTime").plus(rawOffSet).`as`("date")
        val match = Aggregation.match(Criteria.where("date").gte(newfromDate).lte(newtoDate))
        val project2=Aggregation.project("name").and("date").dateAsFormattedString("%Y-%m-%d").`as`("date")
        val group = Aggregation.group("date", "name").count().`as`("count")
        val sort = Aggregation.sort(Sort.Direction.ASC, "_id.date")

        val aggregation = Aggregation.newAggregation(project1, match, project2,group, sort)

        var result = mongoTemplate.aggregate(aggregation, "${clientId}_event", EventCount::class.java)
//        var newformat=SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
//        result.forEach { action->
//            var date=action.date
//            var newdate=newformat.parse(date)
//            var c=Calendar.getInstance()
//            c.time=newdate
//            action.date="${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DATE)}"
//
//         }

        return result.mappedResults
    }

}