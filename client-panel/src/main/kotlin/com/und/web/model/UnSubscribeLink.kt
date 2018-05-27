package com.und.web.model

import javax.validation.constraints.NotNull

class UnSubscribeLink {

    @NotNull(message = "link cant be empty")
    lateinit var unSubscribeLink:String

}