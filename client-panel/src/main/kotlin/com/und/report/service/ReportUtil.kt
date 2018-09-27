package com.und.report.service

import com.und.web.model.DataType
import com.und.web.model.GlobalFilter
import com.und.web.model.GlobalFilterType
import com.und.web.model.Unit

class ReportUtil {

    companion object {

        fun buildFilter(globalFilterType: GlobalFilterType, name: String, type: DataType, operator: String, values: List<String>, valueUnit: Unit?): GlobalFilter {
            var filter = GlobalFilter()
            if(globalFilterType != null) filter.globalFilterType = globalFilterType
            if (name != null) filter.name = name
            if (type != null) filter.type = type
            if (operator != null) filter.operator = operator
            if (values != null) filter.values = values
            if (valueUnit != null) filter.valueUnit = valueUnit
            return filter
        }
    }


}