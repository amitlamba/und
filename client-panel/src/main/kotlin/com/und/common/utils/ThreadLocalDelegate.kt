package com.und.common.utils


import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class ThreadLocalDelegate<T> (val local: ThreadLocal<T>)
    : ReadWriteProperty<Any, T>
{
    companion object {
        fun <T> late_init ()
                = ThreadLocalDelegate<T>(ThreadLocal())
    }

    constructor (initial: T):
            this(ThreadLocal.withInitial { initial })

    constructor (initial: () -> T):
            this(ThreadLocal.withInitial(initial))

    override fun getValue
            (thisRef: Any, property: KProperty<*>): T
            = local.get()

    override fun setValue
            (thisRef: Any, property: KProperty<*>, value: T)
            = local.set(value)
}

typealias thread_local<T> = ThreadLocalDelegate<T>

operator fun <T> ThreadLocal<T>.provideDelegate
        (self: Any, prop: KProperty<*>)
        = ThreadLocalDelegate(this)
