package com.und.common.utils

import com.und.web.model.UserDateInput
import org.springframework.beans.factory.annotation.Autowired
import java.text.SimpleDateFormat
import java.util.*

class CompareDate {

    companion object {
        @JvmStatic
        fun getDate(userDateInput: UserDateInput):UserDateInput{
            var fromDate:String?=userDateInput.fromDate
            var toDate:String?=userDateInput.toDate
            var calender=Calendar.getInstance()

            var newFromDate: Date
            var newToDate: Date
            var format=SimpleDateFormat("yyyy-MM-dd")
            var currentDate= Date()

                                                     //if both date are null set to default 3 month
            if(fromDate==null&&toDate==null){
                newToDate=currentDate
                calender.time=newToDate
                calender.add(Calendar.MONTH,-3)
                newFromDate=calender.time
                fromDate=format.format(newFromDate)
                toDate=format.format(newToDate)
            }else if(toDate==null){                 //if fromDate is given then set toDate 3 month from fromDate
                calender.time=format.parse(fromDate)
                calender.add(Calendar.MONTH,3)
                newToDate=calender.time
                if(newToDate.after(currentDate)){
                    toDate=format.format(currentDate)
                }else{
                    toDate=format.format(newToDate)
                }

            }else if(fromDate==null){               //if fromDate is null then give only one day result
                fromDate=toDate
            }else{
                newFromDate=format.parse(fromDate)
                newToDate=format.parse(toDate)
                if(newFromDate.after(currentDate)){
                    newFromDate=currentDate
                }
                if(newFromDate.after(newToDate)) {
                    newToDate = currentDate
                }
            }

            userDateInput.fromDate=fromDate
            userDateInput.toDate=toDate
            return userDateInput
    }

    }
}