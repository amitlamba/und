package com.und.model.jpa

import com.und.web.model.RunType
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name="ab_camapign")
class AbCampaign {

    @Id
    var id:Long?=null
    @Column(name="campaign_id",nullable = false)
    @JoinColumn(name = "campaign_id")
    lateinit var campaign:Campaign  //one to one
    @NotNull
    @Column(name = "run_type")
    var runType:RunType = RunType.AUTO
    @NotNull
    @Column(name="rewind")
    var rewind:Boolean =false
    @Column(name="wait_time")
    var waitTime:Int?=null     //in minutes
    @Column(name="sample_size")
    var sampleSize:Int?=null            //need to check we use it or not.
}


@Entity
@Table(name="variant")
class Variant {
    @Id
    var id:Long?=null
    //add campaign id if you want bi directional mapping
    @NotNull
    @Column(name="percentage",nullable = false)
    var percentage:Int?=null
    @NotNull
    @Column(name="name")
    var name:String?=null
    @Column(name="users")
    var users:Int?=null
    @Column(name="winner")
    @NotNull
    var winner:Boolean=false
    @NotNull
    @Column(name="template_id",nullable = false)
    var templateId:Int?=null
}