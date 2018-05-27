package com.und.model.utils

import java.time.LocalDateTime


class Segment {

    var id: Long? = null

    var name: String = ""
    var type: String = ""
    var creationDate: LocalDateTime = LocalDateTime.now()
    var conversionEvent: String? = null
    var didEvents: DidEvents? = null
    var didNotEvents: DidEvents? = null
    var globalFilters: List<GlobalFilter> = listOf()
    var geographyFilters: List<Geography> = listOf()
}

class DidEvents {
    var description: String? = null
    var joinCondition: JoinCondition = JoinCondition()
    var events: List<Event> = listOf()
}

class JoinCondition {
    var anyOfCount: Long? = null
    var conditionType: String? = null// AllOf / AnyOf
}

class Event {
    var name: String = ""
    var dateFilter: DateFilter = DateFilter()
    var propertyFilters: List<PropertyFilter> = listOf()
    var whereFilter: WhereFilter = WhereFilter()
}

class DateFilter {
    var operator: DateOperator = DateOperator.After
    var values: List<String> = listOf()
    var valueUnit: String = ""
}

class PropertyFilter {
    var name: String = ""
    var type: PropertyType = PropertyType.string
    var filterType: PropertyFilterType = PropertyFilterType.UTM
    var operator: String = ""
    var values: List<String> = listOf()
    var valueUnit: String = ""
}

enum class PropertyType {
     string ,
     number,
     date
}

enum class PropertyFilterType {
     eventproperty,
     genericproperty,
     UTM
}

class WhereFilter {
    var whereFilterName: WhereFilterName = WhereFilterName.Count
    var propertyName: String = ""
    var operator: NumberOperator = NumberOperator.Between
    var values: List<Long> = listOf()
}

enum class WhereFilterName {
     Count,
     SumOfValuesOf
}

enum class DateOperator {
    Before,
    After,
    On,
    Between,
    InThePast,
    WasExactly,
    Today,
    InTheFuture,
    WillBeExactly
}

enum class NumberOperator {
    Equals ,
    Between,
    GreaterThan,
    LessThan,
    NotEquals,
    Exists,
    DoesNotExist
}

enum class StringOperator {
    Equals ,
    NotEquals,
    Contains,
    DoesNotContain ,
    Exists,
    DoesNotExist
}

class GlobalFilter {
    var globalFilterType: GlobalFilterType = GlobalFilterType.AppFields
    var name: String = ""
    var type: String = ""
    var operator: String = ""
    var values: Array<Any> = arrayOf()
    var valueUnit: String = ""
}

enum class GlobalFilterType(val type: String) {
    UserProperties("UserProperties"),
    Demographics("Demographics"),
    Technographics("Technographics"),
    Reachability("Reachability"),
    AppFields("appFields")
}

class Geography {
    var country: Country = Country()
    var state: State = State()
    var city: City = City()
}

class Country {
    var id: Long = 0
    var name: String = ""
}

class State {
    var id: Long = 0
    var name: String = ""
}

class City {
    var id: Long = 0
    var name: String = ""
}

class RegisteredEvent {
    var name: String = ""
    var properties: List<RegisteredEventProperties> = listOf()
}

class RegisteredEventProperties {
    var name: String = ""
    var dataType: String = ""
    var regex: String = ""
    var options: Array<Any> = arrayOf()
}

class GlobalFilterItem {
    var value: String = ""
    var displayName: String = ""
    var type: String = ""
}
