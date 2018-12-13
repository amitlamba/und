package com.und.web.controller.exception

class CustomException:RuntimeException {

    constructor():super()
    constructor(message:String):super(message)
    constructor(message: String,cause:Throwable):super(message, cause)

}