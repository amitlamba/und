package com.und.scheduler

import org.junit.Test
import org.quartz.CronExpression

class CronExpressionTest {

    @Test
    fun testInvalidCron() {
        val cron = "0 0 10 ? 1/1 SATL *"
        CronExpression.validateExpression(cron)
    }
}