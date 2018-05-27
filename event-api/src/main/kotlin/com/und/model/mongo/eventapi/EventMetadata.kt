package com.und.model.mongo.eventapi

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "#{tenantProvider.getTenant()}_eventmetadata")
class EventMetadata {
    @field: Id
    var id: String? = null
    var name: String? = null

    val properties: MutableList<Property> = mutableListOf()
}


@Document(collection = "#{tenantProvider.getTenant()}_userproperties")
class CommonMetadata {
    @field: Id
    var id: String? = null
    var name: String? = null
    val properties: MutableList<Property> = mutableListOf()
}


class Property {
    var dataType:DataType = DataType.string
    var regex:String? = null
    var name: String? = null
    val options: MutableSet<Any> = mutableSetOf()
}

enum class DataType{
    string,
    number,
    date,
    range,
    boolean
}