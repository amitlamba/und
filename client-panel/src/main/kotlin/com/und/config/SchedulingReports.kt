package com.und.config

import com.und.report.service.ReachabilityService
import com.und.report.service.ReachabilityServiceImpl
import com.und.service.ClientService
import com.und.service.EventService
import com.und.service.EventUserService
import com.und.service.SegmentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
@ComponentScan("com.und.service","com.und.report.service")
class SchedulingReports {

    @Autowired
    lateinit var clientService:ClientService
    @Autowired
    lateinit var eventUserService:EventUserService
    @Autowired
    lateinit var eventService: EventService
    @Autowired
    @Qualifier("reachabilityiservicempl")
    lateinit var segmentReachService: ReachabilityService

    @Autowired
    lateinit var segmentService : SegmentService

//    @Scheduled(cron = " */10 * * * * *")
    //@Scheduled(cron = " 0 0 24 * * * *",initialDelay = 0)
    fun createReport(){
        /*
         * 1-find total no of client  --select count(*) as 'Total client' from clienttable
         * 2-Details of new client on last day   --select name from client where creationDate=todatDate
         * 3-Total no of templete --select count(*) as 'Total templete' from templeteTable.
         * 4-Details of new templete  --select templetename from templete where creationDate=todayDate
         * 5-find total no of campiagn  --select count(*) as 'Total campaign' from campiagntable
         * 6-Details of new campiagn on last day   --select name from campiagntable where creationDate=todatDate
         * 7-Total EventUser on last day  --db.eventuser.find({}).count(); ok
         * 8- Event on last day           --db.{variable}_event.count();
         * 9- No of highest event in which account or by which client
         */


        //Task 1 and 2
        println("scheduler running every 10 th second")
        var totalClient=clientService.getClientCount()
        var newClient=clientService.getNewClient()

        println("\n total client $totalClient \t total new client $newClient")

        // Task 7
        var totalEventUser=eventUserService.getTotalEventUserToday()
        print("\n totalevevt   $totalEventUser")

        //Task 8 and 9

        var lastDayEvent=eventService.getTotalEventToday()
        var maxEventUser=eventService.getUserWithMaxEvent()
        print("\nmaxeventuser $maxEventUser   \tlastdayevent $lastDayEvent")

    }

    @Scheduled(cron="0 0 6 * * ?")
    fun setSegmentReachability(){
        val clients=clientService.getClients()
        clients.forEach {
            it.id?.let {
                var clientId=it
                if(it>1000){
                    val segments= segmentService.segmentByClientId(it)
                    segments.forEach {
                        it.id?.let {
                            segmentReachService.setReachabilityOfSegmentToday(it,clientId)
                        }
                    }
                }
            }
        }
    }
}