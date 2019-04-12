package com.und.common.utils

import com.und.model.mongo.EventUser

class ReplaceNullPropertyOfEventUser {

    companion object {
        fun replaceNullPropertyOfEventUser(eventUser: EventUser?, variable: Set<String>):EventUser? {
            eventUser?.let {
                val user = it
                variable.forEach {
                    when (it) {
                        "\${user.identity.email}" -> {
                            if (user.identity.email == null) user.identity.email = ""
                        }

                        "\${user.identity.mobile}" -> {
                            if (user.identity.mobile == null) user.identity.mobile = ""
                        }

                        "\${user.identity.fbId}" -> {
                            if (user.identity.fbId == null) user.identity.fbId = ""
                        }

                        "\${user.identity.googleId}" -> {
                            if (user.identity.googleId == null) user.identity.googleId = ""
                        }
                        "\${user.standardInfo.firstname}" -> {
                            if (user.standardInfo.firstname == null) user.standardInfo.firstname = ""
                        }
                        "\${user.standardInfo.lastname}" -> {
                            if (user.standardInfo.lastname == null) user.standardInfo.lastname = ""
                        }
                        "\${user.standardInfo.gender}" -> {
                            if (user.standardInfo.gender == null) user.standardInfo.gender = ""
                        }
                        "\${user.standardInfo.dob}" -> {
                            if (user.standardInfo.dob == null) user.standardInfo.dob = ""
                        }
                        "\${user.standardInfo.country}" -> {
                            if (user.standardInfo.country == null) user.standardInfo.country = ""
                        }
                        "\${user.standardInfo.City}" -> {
                            if (user.standardInfo.City == null) user.standardInfo.City = ""
                        }
                        "\${user.standardInfo.Address}" -> {
                            if (user.standardInfo.Address == null) user.standardInfo.Address = ""
                        }
                    }
                }
                return user
            }
            return eventUser
        }
    }


}