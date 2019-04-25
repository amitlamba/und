package com.und.common.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisUtilityService {

    @Autowired
    private lateinit var redisTemplalte:RedisTemplate<String,Int>

    fun storingQueueOfTemplateIdToRedis(key:String,listOfTemplateId:MutableList<Int>){
        redisTemplalte.opsForList().leftPushAll(key,listOfTemplateId)
    }
    fun gettingFirstTemplateIdInQueue(key: String):Int{
        return redisTemplalte.opsForList().leftPop(key) ?: 0
    }
    fun addingTheTemplateIdToEndOfQueue(key: String,templateId:Int){
        redisTemplalte.opsForList().rightPush(key,templateId)
    }
    fun gettingWinnerTemplateForThisCampaign(key: String):Int?{
        return redisTemplalte.opsForValue().get(key)
    }
    fun storingSampleSizeToRedis(key:String,size:Int){
        redisTemplalte.opsForValue().set(key,size)
    }
    fun decreasingTheSampleSize(key: String,step:Long){
        redisTemplalte.opsForValue().increment(key,step)
    }
    fun gettingSampleSize(key: String):Int{
       return redisTemplalte.opsForValue().get(key) ?: 0
    }
    fun settingWinnerTemplateForThisCampaign(key: String,templateId: Int){
        redisTemplalte.opsForValue().set(key,templateId)
    }

}