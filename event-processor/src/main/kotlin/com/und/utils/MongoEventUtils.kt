package com.und.utils

import org.springframework.stereotype.Component
import java.util.HashMap

@Component
class MongoEventUtils {

     fun toDateInMap(attributes: HashMap<String, Any>): HashMap<String, Any> {
        val dateUtil = DateUtils()
        val outMap: HashMap<String, Any> = HashMap()
        attributes.forEach { key, value ->
            outMap += when (value) {
                is String -> (key to dateUtil.parseToDate(value))
                else -> (key to value)
            }
        }
        return outMap
    }
}