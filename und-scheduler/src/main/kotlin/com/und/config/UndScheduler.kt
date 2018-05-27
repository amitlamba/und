package com.und.config

import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import java.io.IOException
import javax.annotation.PostConstruct
import javax.sql.DataSource


@Configuration
@ConditionalOnExpression("'\${using.spring.schedulerFactory}'=='true'")
class UndScheduler {

    internal var logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var applicationContext: ApplicationContext

    @Autowired
    lateinit var dataSource: DataSource




    @PostConstruct
    fun init() {
        logger.info("starting Quartz...")
    }

    @Bean
    fun springBeanJobFactory(): SpringBeanJobFactory {
        val jobFactory = AutoWiringSpringBeanJobFactory()
        logger.debug("Configuring Job factory")

        jobFactory.setApplicationContext(applicationContext)
        return jobFactory
    }


    @Bean
    fun schedulerFactory(applicationContext: ApplicationContext,
                         dataSource: DataSource): SchedulerFactoryBean {
        val schedulerFactoryBean = SchedulerFactoryBean()
        schedulerFactoryBean.setDataSource(dataSource)
        schedulerFactoryBean.setConfigLocation(ClassPathResource("quartz.properties"))
        schedulerFactoryBean.setJobFactory(springBeanJobFactory())
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext")
        return schedulerFactoryBean
    }

    @Bean
    @Throws(SchedulerException::class, IOException::class)
    fun scheduler(): Scheduler {

        val factory = schedulerFactory(applicationContext,dataSource)
        //factory.initialize(ClassPathResource("quartz.properties").inputStream)

        logger.debug("Getting a handle to the Scheduler")
        val scheduler = factory.scheduler
        scheduler.setJobFactory(springBeanJobFactory())
        //scheduler.scheduleJob(job, trigger)

        logger.debug("Starting Scheduler threads")
        //scheduler.start()
        return scheduler
    }



}