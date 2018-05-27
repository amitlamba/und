package com.und.common.utils

import org.slf4j.LoggerFactory


//FIXME add extensions for logger and try to use idiomatic property delegates
fun <T> loggerFor(clazz: Class<T>) = LoggerFactory.getLogger(clazz)





