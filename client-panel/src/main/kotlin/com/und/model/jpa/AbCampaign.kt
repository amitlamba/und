package com.und.model.jpa

import com.und.web.model.RunType
import javax.persistence.*

@Entity
@Table(name="ab_camapign")
class AbCampaign {

    @Id
    var id:Long?=null
    @Column(name="campaign_id",nullable = false)

    @JoinColumn(name = "campaign_id")
    lateinit var campaign:Campaign  //one to one
    @Column(name = "run_type")
    var runType:RunType = RunType.AUTO
    @Column(name="rewind")
    var rewind:Boolean =false
    @Column(name="wait_time")
    var waitTime:Int?=null     //in minutes
    @Column(name="sample_size")
    var sampleSize:Int?=null
    @Column(name="live_sample_size")
    var liveSampleSize:Int?=null   //optional we are taking this info in variant also

}


@Entity
@Table(name="variant")
class Variant {
    @Id
    var id:Long?=null
    @Column(name="percentage",nullable = false)
    var percentage:Int?=null
    @Column(name="name")
    var name:String?=null
    @Column(name="users")
    var users:Int?=null
    @Column(name="winner")
    var winner:Boolean=false
    @Column(name="template_id",nullable = false)
    var templateId:Int?=null
}