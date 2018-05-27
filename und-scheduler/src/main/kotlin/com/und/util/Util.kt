package com.und.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory


//FIXME add extensions for logger and try to use idiomatic property delegates
fun <T> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)

fun Logger.debugT(msg: String) = if (isDebugEnabled) this.debug(msg) else Unit



