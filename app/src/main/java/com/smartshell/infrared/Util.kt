package com.smartshell.infrared

import kotlin.experimental.and

/**
 * ByteArray转化为16进制字符串
 */
fun ByteArray.toHex(fromTo: Pair<Int, Int> = 0 to this.size): String {
    val sb = StringBuilder()
    (fromTo.first until fromTo.second).forEach {
        sb.append(
            String.format(
                "%02X",
                this[it].and(0xFF.toByte())
            )
        )
    }
    return sb.toString()
}

/**
 * ByteArray转化为16进制字符串
 */
fun ByteArray.toHexString(fromTo: Pair<Int, Int> = 0 to this.size): String {
    val sb = StringBuilder()
    (fromTo.first until fromTo.second).forEach {
        sb.append(
            String.format(
                "%02X",
                this[it].and(0xFF.toByte())
            ) + " "
        )
    }
    return sb.toString()
}