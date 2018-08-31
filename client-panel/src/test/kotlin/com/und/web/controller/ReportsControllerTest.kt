package com.und.web.controller

import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ReportsControllerTest {

    private val fromDate:String?=null
    private val toDate:String="2017-01-08"
    @Test
    fun getEventsCount() {

        var isValidDate:Boolean=false
        var pattern: Pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$")
        if (pattern.matcher(fromDate).matches() && pattern.matcher(toDate).matches()) {
        try {

                val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                format.isLenient = false
                var newFromDate: Date = format.parse(fromDate)
                print(newFromDate)
                var newToDate: Date = format.parse(toDate)
                print(newToDate)

                print(newFromDate.before(newToDate))
        }catch (e :Exception){
           print(e.message)
        }}
        else{
            print("not valid")
        }


    }
    @Test
    fun anotherTest(){

        var currentDate= Date()

        var format:SimpleDateFormat= SimpleDateFormat("yyyy-MM-dd")
        var strCurrentDate=format.format(currentDate)
        currentDate=format.parse(strCurrentDate)
        var newFromDate:Date
        var newToDate:Date

        if(toDate!=null){
            newToDate=format.parse(toDate)
            print(newToDate)
        }else{
            newToDate=currentDate
        }
        if(fromDate!=null){
            newFromDate=format.parse(fromDate)
        }else{
            var calender=Calendar.getInstance()

            calender.time=newToDate
            println(calender.time)
            calender.add(Calendar.MONTH,-3)


            println(calender.time)
            newFromDate=calender.time
            //print(format.)
        }

        println(format.format(newFromDate))
        print(format.format(newToDate))
    }



}