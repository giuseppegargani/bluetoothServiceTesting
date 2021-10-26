package com.example.bluetoothtesting

/**
 * Created by Giuseppe Gargani on 26/10/21 based on Ramankit
 */

data class DeviceData(val deviceName: String?,val deviceHardwareAddress: String){

    override fun equals(other: Any?): Boolean {
        val deviceData = other as DeviceData
        return deviceHardwareAddress == deviceData.deviceHardwareAddress
    }

    override fun hashCode(): Int {
        return deviceHardwareAddress.hashCode()
    }

}