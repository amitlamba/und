//package com.und.common.utils
//
//import com.und.model.mongo.eventapi.DataType
//import com.und.model.mongo.eventapi.Property
//import java.time.LocalDate
//import java.time.LocalDateTime
//import java.util.*
//
//object MetadataUtil {
//
//    fun buildMetadata(attributes: HashMap<String, Any>, properties: MutableList<Property>): MutableList<Property> {
//        val outProperties: MutableList<Property> = mutableListOf()
//        attributes.forEach { key, value ->
//            val existingProperty = properties.find { it.name == key }
//            val property = Property()
//            if (existingProperty != null) {
//                property.options.addAll(existingProperty.options)
//            }
//            property.options.add(value)
//            property.name = key
//            outProperties += property
//        }
//        outProperties.forEach { property ->
//            property.dataType = dataType(property.options)
//            if (property.dataType in setOf(DataType.date, DataType.number)) {
//                property.options.clear()
//            }
//            property.regex = null
//        }
//
//        return outProperties
//    }
//
//    private fun dataType(options: MutableSet<Any>): DataType {
//
//        fun findDataType(value: Any?): DataType {
//            return when (value) {
//                is Number, is Int, is Float, is Double -> DataType.number
//                is Date, is LocalDate, is LocalDateTime -> DataType.date
//                is Array<*> -> {
//                    if (value.isNotEmpty()) findDataType(value[0]) else DataType.string
//                }
//                else -> DataType.string
//            }
//        }
//
//        val dataTypes = mutableSetOf<DataType>()
//        options.forEach {
//            dataTypes.add(findDataType(it))
//        }
//        //if multiple data types are found default to string
//        return if (dataTypes.size == 1) dataTypes.first() else DataType.string
//    }
//}