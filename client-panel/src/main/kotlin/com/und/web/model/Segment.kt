package com.und.web.model

import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


class Segment : Serializable {

    var id: Long? = null

    @NotNull
    @Size(min = 2, max = 50)
    @Pattern(regexp = "[A-Za-z0-9-_][A-Za-z0-9-_\\s]*")
    var name: String = ""

    @NotNull
    var type: String = ""
    var creationDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    var conversionEvent: String? = null
    var didEvents: DidEvents? = null
    var didNotEvents: DidEvents? = null
    var globalFilters: List<GlobalFilter> = listOf()
    var geographyFilters: List<Geography> = listOf()
}

class DidEvents : Serializable {
    var description: String? = null
    var joinCondition: JoinCondition = JoinCondition()
    var events: List<Event> = listOf()
}

class JoinCondition : Serializable {
    var anyOfCount: Long? = null
    var conditionType: ConditionType = ConditionType.AllOf // AnyOf
}

enum class ConditionType : Serializable {
    AllOf,
    AnyOf
}

class Event : Serializable {
    var name: String = ""
    var dateFilter: DateFilter = DateFilter()
    var propertyFilters: List<PropertyFilter> = listOf()
    var whereFilter: WhereFilter? = null
}

class DateFilter : Serializable {
    var operator: DateOperator = DateOperator.After
    var values: List<String> = listOf()
    var valueUnit: Unit = Unit.NONE
}

class PropertyFilter : Serializable {
    var name: String = ""
    var type: DataType = DataType.string
    private var _filterType: PropertyFilterType? = null
    var filterType: PropertyFilterType? = null
        get() {
            if (_filterType == null) {
                val genericProperties = genericProperty.values().map { it.desc }
                val uTMProperties = utmProperty.values().map { it.desc }
                _filterType = when {
                    genericProperties.contains(name) -> PropertyFilterType.genericproperty
                    uTMProperties.contains(name) -> PropertyFilterType.UTM
                    else -> PropertyFilterType.eventproperty
                }

            }
            return _filterType
        }

    var operator: String = ""
    var values: List<String> = listOf()
    var valueUnit: Unit = Unit.NONE
}


enum class PropertyFilterType : Serializable {
    eventproperty,
    genericproperty,
    UTM
}


class WhereFilter : Serializable {
    var whereFilterName: WhereFilterName? = null
    var propertyName: String = ""
    var operator: NumberOperator = NumberOperator.NONE
    var values: List<Long>? = null
}

enum class WhereFilterName : Serializable {
    Count,
    SumOfValuesOf
}

enum class AggregationType : Serializable {
    Avg,
    Sum
}

enum class DateOperator : Serializable {
    Before,
    After,
    On,
    Between,
    InThePast,
    WasExactly,
    Today,
    InTheFuture,
    WillBeExactly,
    Exists,
    DoesNotExist,
    NONE
}

enum class NumberOperator : Serializable {
    Equals,
    Between,
    GreaterThan,
    LessThan,
    NotEquals,
    Exists,
    DoesNotExist,
    NONE
}

enum class StringOperator : Serializable {
    Equals,
    NotEquals,
    Contains,
    DoesNotContain,
    Exists,
    DoesNotExist,
    NONE
}

enum class Unit : Serializable {
    mins,
    hours,
    days,
    week,
    month,
    year,
    NONE
}

class GlobalFilter : Serializable {
    var globalFilterType: GlobalFilterType = GlobalFilterType.AppFields
    var name: String = ""
    var type: DataType = DataType.string
    var operator: String = ""
    var values: List<String> = mutableListOf()
    var valueUnit: Unit = Unit.NONE
}

enum class GlobalFilterType(val type: String) : Serializable {
    UserProperties("UserProperties"),
    Demographics("Demographics"),
    Technographics("Technographics"),
    Reachability("Reachability"),
    AppFields("AppFields"),

    EventProperties("EventProperties"),
    EventAttributeProperties("EventAttributes"),
    EventTimeProperties("EventTimeProperties"),
    EventComputedProperties("EventComputed"),
    UserComputedProperties("UserComputed")
}

class Geography : Serializable {
    var country: Country? = null
    var state: State? = null
    var city: City? = null
}

class Country(val id: Int, val name: String) : Serializable

class State(val id: Int, val name: String) : Serializable

class City(val id: Int, val name: String) : Serializable


class RegisteredEvent : Serializable {
    var name: String = ""
    var properties: List<RegisteredEventProperties> = listOf()
}

class RegisteredEventProperties : Serializable {
    var name: String = ""
    var dataType: DataType = DataType.string
    var regex: String = ""
    var options: Array<Any> = arrayOf()
}

class GlobalFilterItem : Serializable {
    var value: String = ""
    var displayName: String = ""
    var type: String = ""
}


enum class DataType : Serializable {
    string,
    number,
    date,
    range,
    boolean
}

enum class genericProperty(val desc: String) : Serializable {
    TimeOfDay("Time of day"),
    FirstTime("First Time"),
    DayOfWeek("Day of week"),
    DayOfMonth("Day of month");

}

enum class utmProperty(val desc: String) : Serializable {
    UTMSource("UTM Source"),
    UTMVisited("UTM Visited"),


}