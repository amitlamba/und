package com.und.scheduler

import com.und.model.FixedDateCalendar
import org.junit.Test
import org.quartz.*
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.impl.StdSchedulerFactory
import java.time.LocalDate


class TestFixedDateCalendar {


    @Test
    fun testFixedDates1() {

        try {
            // Grab the Scheduler instance from the Factory
            val scheduler = StdSchedulerFactory.getDefaultScheduler()
            val dates = mutableListOf(LocalDate.of(2018, 5, 14), LocalDate.of(2018, 5, 15), LocalDate.now())
            val cal = FixedDateCalendar(dates)
            scheduler.addCalendar("myC", cal, false, true)

            val job = JobBuilder.newJob(HelloJob::class.java)
                    .withIdentity("job1", "group1")
                    .build()

            val cron = ""
            val trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(5)
                            .withRepeatCount(3))
                    .modifiedByCalendar("myC")
                    .build()


            scheduler.scheduleJob(job, trigger)
            // and start it off
            scheduler.start()
            Thread.sleep(20000)
            println("finished")
            //scheduler.shutdown()

        } catch (se: SchedulerException) {
            se.printStackTrace()
        }


    }

    class HelloJob : Job {
        override fun execute(p0: JobExecutionContext?) {
            println("Hello bhai hello brother")
        }
    }
}