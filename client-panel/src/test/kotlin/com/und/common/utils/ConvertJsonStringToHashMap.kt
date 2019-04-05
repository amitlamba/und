package com.und.common.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test

class ConvertJsonStringToHashMap {

    var jsonString="{\"key1\":\"value1\",\"key2\":\"value2\"}"
    var mapper=ObjectMapper()

    @Test
    fun parseJsonToMap(){
        var map= HashMap<String,String>()
        var node: JsonNode =mapper.readTree(jsonString)
        var entity=node.fields()
        entity.forEach {
            println("${it.key} ${it.value}")
        }
        println(node.get("key1"))
        print(node.get("key2"))
//        mapper.readValue(jsonString,TypeReference<HashMap<String,String>>(){})

    }
}