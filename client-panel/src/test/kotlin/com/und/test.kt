package com.und

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*
import kotlin.collections.ArrayList

object test {
    @JvmStatic
    fun main(args: Array<String>) {
        val dates = Arrays.asList("2018-08-27", "2018-08-28", "2018-08-29")
        val mapper = ObjectMapper()
        //open file for append
        var file=PrintWriter(FileWriter("testdata",true),true)
        for (j in 0..2 step 1) {
            val data = Data()
            data.date = dates.get(2)
            data.userCountData = ArrayList()
            var lsit=ArrayList<UserCountData>()
            var i = 0
            while (i <= 288) {
                val obj = UserCountData()
                obj.newusercount = 10000 + (i+2) * i
                obj.oldusercount = 15000 + (i+2) * i
                obj.time = i
                lsit.add(obj)
                i += 5
            }
            data.userCountData=lsit
            //save object in file
            mapper.writeValue(file,data)

        }
        file.close()
    }
}


internal class Data {
    lateinit var date: String
    lateinit var userCountData: List<UserCountData>
}

internal class UserCountData {
    var newusercount: Int = 0
    var oldusercount: Int = 0
    var time: Int = 0
}