package com.und.web.model

class Reachability {
    var totalUser: Int? =null
    var email: Long = 0
    var sms: Long = 0
    var webpush: Long = 0
    var android: Long = 0
    var ios: Long = 0

}

class ReachabilityResult{
    var emailCount:List<Long> = emptyList()
    var mobileCount:List<Long> =emptyList()
    var androidCount:List<Long> =emptyList()
    var webCount:List<Long> = emptyList()
    var iosCount:List<Long> = emptyList()
}