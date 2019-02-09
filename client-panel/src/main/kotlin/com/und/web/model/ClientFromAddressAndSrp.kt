package com.und.web.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

class ClientFromAddressAndSrp {

     var settings:Map<String,List<Long>> = emptyMap()
}

@Entity
class ClientEmailSettIdFromAddrSrp{
//     @Column(name="from_address")
     var fromAddress:String?=null
     @Id
     var ceid:Long?=null
//     @Column(name="srp_name")
     var srpName:String?=null
}