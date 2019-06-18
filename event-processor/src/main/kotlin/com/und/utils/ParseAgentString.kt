package com.und.utils

import eu.bitwalker.useragentutils.OperatingSystem
import eu.bitwalker.useragentutils.UserAgent
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("ParseAgentString")

fun systemDetails(agentString: String): SystemDetails {
    val systemDetails = SystemDetails()
    val userAgent = UserAgent.parseUserAgentString(agentString)
    val operatingSystem = userAgent.operatingSystem

//    val browser = userAgent.browser
//    systemDetails.browser = browser.getName()
//    systemDetails.browserVersion = userAgent.browserVersion?.version
//
//    val deviceType = operatingSystem.deviceType
//    val osName = operatingSystem.getName()
//    systemDetails.OS = osName
//    systemDetails.deviceType = deviceType.getName()

    var browserInfo=parseBrowser(userAgent)
    var osInfo= parseOS(operatingSystem)
    systemDetails.OS=osInfo.first
    systemDetails.osVersion=osInfo.second
    systemDetails.browser=browserInfo.first
    systemDetails.browserVersion=browserInfo.second
    systemDetails.deviceType= parseDevice(operatingSystem)

    return systemDetails
}

data class SystemDetails(
        var OS: String? = null,
        var osVersion:String?=null,
        var browser: String? = null,
        var browserVersion: String? = null,
        var deviceType: String? = null, //mobile, tablet, laptop etc
        var deviceVersion:String?=null,
        var agentString: String? = null
)

private fun parseBrowser(userAgent: UserAgent):Pair<String,String?>{
    var browserName=userAgent.browser.name
    var browserVersion=userAgent.browserVersion?.version
    logger.info("Browser Name  $browserName version $browserVersion")
    when{
        browserName.contains("CHROME") -> return Pair("Chrome",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
        browserName.contains("FIREFOX") -> return Pair("Firefox",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
        browserName.contains("EDGE") -> return Pair("Microsoft Edge",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
        browserName.contains("SAFARI") -> return Pair("Safari",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
        browserName.contains("IE") -> return Pair("Internet Explorer",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
        browserName.contains("OPERA") -> return Pair("Opera",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
        browserName.contains("THUNDERBIRD") -> return Pair("Thunderbird",browserVersion?.replace(regex = Regex("\\..*"),replacement = ""))
    }
    return Pair(browserName,null)
}

private fun parseOS(os: OperatingSystem):Pair<String,String?>{
    var osName=os.getName()
    logger.info("Os name $osName")
    when{
        osName.contains("Windows") -> {
            var info=osName.split(" ")
            if (info.size==2){
                return Pair(info[0],info[1])
            }
            else if (info.size==3){
                if(info[1].equals("10"))
                    return Pair(info[0]+"Phone",info[1])
                else
                    return Pair(info[0]+info[1],info[2])
            }
            else{
                return Pair(info[0],null)
            }
        }
        osName.contains("Mac OS X") -> {
            return Pair("Mac OS","X")
        }
        osName.contains("Android") -> {
            var info=osName.split(" ")
            if(info.size==1)
                return Pair(info[0],null)
            else
                return Pair(info[0],info[1].replace(regex = Regex("\\..*"),replacement = ""))
        }
        osName.contains("iOS") -> {
            var info=osName.split(" ")
            if(info.size>=2) return Pair(info[0],info[1].replace(regex = Regex("\\..*"),replacement = ""))
        }
        osName.contains("Linux") ->{
            return Pair("Linux",null)
        }
        osName.contains("Ubuntu") -> {
            return Pair("Ubuntu",null)
        }
    }
    return Pair(osName,null)
}

private fun parseDevice(os: OperatingSystem):String{
    return os.deviceType.getName()
}
