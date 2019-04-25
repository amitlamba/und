package com.und.model.jpa

import com.und.web.model.RunType
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name="ab_campaign")
class AbCampaign {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "abcampaign_seq_generator")
    @SequenceGenerator(name="abcampaign_seq_generator",sequenceName = "ab_campaign_seq_id")
    var id:Long?=null
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name="campaign_id")
    var campaign:Campaign?=null
    @NotNull
    @Column(name = "run_type")
    var runType:RunType = RunType.AUTO
    @NotNull
    @Column(name="remind")
    var remind:Boolean = true
    @Column(name="wait_time")
    var waitTime:Int?=null     //in minutes
    @Column(name="sample_size")
    var sampleSize:Int?=null
}


@Entity
@Table(name="variant")
class Variant {
    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "variant_seq_generator")
    @SequenceGenerator(name="variant_seq_generator",sequenceName = "variant_seq_id")
    var id:Long?=null
    @NotNull
    @Column(name="percentage",nullable = false)
    var percentage:Int?=null
    @NotNull
    @Column(name="name",nullable = false)
    lateinit var name:String
    @Column(name="users")
    var users:Int?=null
    @Column(name="winner")
    @NotNull
    var winner:Boolean=false
    @NotNull
    @Column(name="template_id",nullable = false)
    var templateId:Int?=null
}