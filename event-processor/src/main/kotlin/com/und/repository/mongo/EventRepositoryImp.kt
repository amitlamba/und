package com.und.repository.mongo

import com.und.model.mongo.Event

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
import java.time.LocalDate

@Repository
class EventRepositoryImp {

    @Autowired
    lateinit var mongoTemplete:MongoTemplate

    private var max:Long=0
    private var clientId:Long=0

    fun getTotalEventToday():Long{
        //run  loop for total no of client
        var startId:Long=2
        var endId:Long=3
        var sum:Long=0
        for(id in startId..endId){

            var temp=getTodayEventById(id)
            sum+=temp

            if(temp>max){
                max=temp
                clientId=id
            }
        }
        return sum
    }

    fun getTodayEventById(clientId:Long):Long{
        var date= LocalDate.of(2018,7,3)
        var year=date.year
        var month=date.monthValue
        var day=date.dayOfMonth



        val format= SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        var newfromDate=format.parse("$year-$month-$day 00:00:00")

        var newtoDate=format.parse("$year-$month-$day 23:29:54")

        var query:Query=Query()
        val addCriteria = query.addCriteria(Criteria.where("creationTime").gte(newfromDate).lte(newtoDate))
        return (mongoTemplete.count(query, Event::class.java,"${clientId}_event"))

    }

    fun getUserWithMaxEvent():String{
        return "clientId : $clientId \t Event perform : $max"
    }
}