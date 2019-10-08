package csg.device.utils

import android.content.Context
import android.content.SharedPreferences

abstract class SmartShellBT : Driver(){
    /*蓝牙地址*/
    abstract var macAddress: String?

    /*获取外设的context对象*/
    abstract var mContext : Context

    companion object{
        public val SPF_MAC_ADDRESS = "bluetooth.mac"
    }


}