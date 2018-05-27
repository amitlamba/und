package com.und.security

import org.springframework.mobile.device.Device
import org.springframework.mobile.device.DevicePlatform

/**
 * Created by shiv on 21/07/17.
 */
class DeviceMock : Device {

    private var normal: Boolean = false
    private var mobile: Boolean = false
    private var tablet: Boolean = false

    override fun isNormal(): Boolean {
        return normal
    }

    override fun isMobile(): Boolean {
        return mobile
    }

    override fun isTablet(): Boolean {
        return tablet
    }

    override fun getDevicePlatform(): DevicePlatform? {
        return null
    }

    fun setNormal(normal: Boolean) {
        this.normal = normal
    }

    fun setMobile(mobile: Boolean) {
        this.mobile = mobile
    }

    fun setTablet(tablet: Boolean) {
        this.tablet = tablet
    }
}
