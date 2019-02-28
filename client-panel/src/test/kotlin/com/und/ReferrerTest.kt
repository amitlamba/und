package com.und

import org.junit.Test
import java.util.regex.Pattern

class ReferrerTest {
    @Test
    fun refrerTest(){
//        var v="https://userndot.com/sdk/js/index.html"
        var v="https://userndot.com"
        var pattern= Pattern.compile("^(?<scheme>https?)(:\\/\\/)(?<host>\\w+(\\.\\w+)+\\/?)")
        var matcher=pattern.matcher(v)
        if(matcher.find()){
            var scheme=matcher.group("scheme")
            var host=matcher.group("host")
            v="$scheme://$host"

            println(v)
        }else{
            print("emd")
//            logger.info("Referer format not match $v")
        }
    }
}