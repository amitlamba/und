package com.und.web.controller

import java.util.*

object Test {
    @JvmStatic
    fun main(args: Array<String>) {
        val mod = Arrays.asList(Mod("type1", "jogi", "singh"), Mod("type2", "jogendra", "shekhawat"))
        var map=mod.groupBy {obj->obj.type }
        println(map.keys)
        print(map.values)
        var list=map.map { action-> action.key }
        print(list)

    }
}

internal class Mod(var type: String, var fname: String, var lname: String)