package com.und.config


import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.SpringBeanJobFactory

/**
 * Adds auto-wiring support to quartz jobs.
  */
class AutoWiringSpringBeanJobFactory : SpringBeanJobFactory(), ApplicationContextAware {

    @Transient
    lateinit var beanFactory: AutowireCapableBeanFactory

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationContext: ApplicationContext) {

        beanFactory = applicationContext.autowireCapableBeanFactory
    }

    @Throws(Exception::class)
    override fun createJobInstance(bundle: TriggerFiredBundle): Any {

        val job = super.createJobInstance(bundle)
        beanFactory.autowireBean(job)
        beanFactory.initializeBean(job,null)
        return job
    }

}