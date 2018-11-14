package com.und.exception

class UndException : RuntimeException {

    constructor() : super()

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    companion object {

        private val serialVersionUID = 5861343547366287163L
    }

}