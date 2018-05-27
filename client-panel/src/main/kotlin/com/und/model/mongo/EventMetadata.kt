package com.und.model.mongo

import com.und.web.model.DataType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "#{tenantProvider.getTenant()}_eventmetadata")
class EventMetadata(
        @field: Id var id: String? = "",
        val name: String,
        val properties: MutableList<Property>) {
}

@Document(collection = "#{tenantProvider.getTenant()}_userproperties")
class CommonMetadata(
        @field: Id var id: String? = "",
        val name: String,
        val properties: MutableList<Property>) {
}


class Property {
    var dataType: DataType = DataType.string
    var regex: String? = null
    var name: String? = null
    val options: MutableSet<Any> = mutableSetOf()
}

enum class DataType {
    string,
    number,
    date,
    range,
    boolean
}