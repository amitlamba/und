package com.und.web.controller

import com.und.common.utils.decrypt
import com.und.common.utils.encrypt
import java.net.URLDecoder
import java.net.URLEncoder
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DateTimeTest {
    companion object {
        @JvmStatic
        fun main(args:Array<String>){
            var localDate1=LocalDateTime.now()
            var localDate2=LocalDateTime.now(ZoneId.of("UTC"))
            println(localDate1)
            println(localDate2)
            var localTime=LocalTime.now()
            println(localTime)
            var parse1=LocalDate.parse("2015-09-09", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            var parse2=LocalDate.parse("2015-11-09")
            println(parse2)
            var datetime=DateTimeFormatter.ISO_DATE_TIME
            datetime= DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                   println( datetime.format(LocalDateTime.now()))


            var timeStamp=System.currentTimeMillis()/1000
            var expiration=5*60
            var current=System.currentTimeMillis()

            //LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toEpochSecond()
            var verificationCode = encrypt("$timeStamp||jogender@gmail.com||3")
            var encodeString=URLEncoder.encode(verificationCode,"UTF-8")
            var emailVerificationLink = "emailVerificationLink" to "https://userndot.com/client/setting/verifyemail?c="+encodeString
            var decodeString=URLDecoder.decode(encodeString,"UTF-8")
            var decrypt= decrypt(decodeString)
            print("$verificationCode  \n $encodeString \n$emailVerificationLink \n$decodeString $decrypt")


            val myDate = "2018/07/31 10:09:00"
            val localDateTime = LocalDateTime.parse(myDate,
                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
/*
  With this new Date/Time API, when using a date, you need to
  specify the Zone where the date/time will be used. For your case,
  seems that you want/need to use the default zone of your system.
  Check which zone you need to use for specific behaviour e.g.
  CET or America/Lima
*/
            val millis = LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()+5*60*1000

           current = LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
            print("$millis \n")
            print("$current \n")


            var c=current-millis
            print(c)

        }
    }


}