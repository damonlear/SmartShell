package com.pemt.detection.extensions

import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * Byte转Int
 */
fun Byte.totInt(): Int = this.toInt().and(0xFF)

/**
 * Bit位转化为Boolean
 */
fun Byte.bitToBoolean(index: Int): Boolean {
    return when (index) {
        7 -> this.toInt().and(0b10000000) == 0b10000000
        6 -> this.toInt().and(0b01000000) == 0b01000000
        5 -> this.toInt().and(0b00100000) == 0b00100000
        4 -> this.toInt().and(0b00010000) == 0b00010000
        3 -> this.toInt().and(0b00001000) == 0b00001000
        2 -> this.toInt().and(0b00000100) == 0b00000100
        1 -> this.toInt().and(0b00000010) == 0b00000010
        0 -> this.toInt().and(0b00000001) == 0b00000001
        else -> throw Throwable("wrong parameters")
    }
}

/**
 * Byte起始count个Bit转Int
 *
 */
fun Byte.sumOfFirst(count: Int): Int {
    val an = when (count) {
        1 -> 0b00000001
        2 -> 0b00000011
        3 -> 0b00000111
        4 -> 0b00001111
        5 -> 0b00011111
        6 -> 0b00111111
        7 -> 0b01111111
        8 -> 0b11111111
        else -> throw Throwable("wrong parameters")
    }
    return this.totInt().and(an)
}

/**
 * Byte末尾count个Bit转Int
 *
 */
fun Byte.sumOfLast(count: Int): Int {
    if (count > 8 || count < 1)
        throw Throwable("wrong parameters")
    return this.totInt().shr(8 - count)
}

/**
 * 跨字节bit计算
 *
 * 第fir.first字节的后fir.second位
 * 与第sed.first的前sed.second位
 */
fun ByteArray.sumOfBit(fir: Pair<Int, Int>, sed: Pair<Int, Int>): Int {
    return this[fir.first].sumOfLast(fir.second).shl(fir.first) +
            this[sed.first].sumOfFirst(sed.second)
}

/**
 * ByteArray中16进制数相加
 */
fun ByteArray.sumOf(fromTo: Pair<Int, Int>): Int {
    return (fromTo.first until fromTo.second).sumBy {
        this[it].totInt().shl((fromTo.second - it - 1) * 8)
    }
}

/**
 * ByteArray中16进制数倒叙相加
 */
fun ByteArray.versaSumOf(fromTo: Pair<Int, Int>): Int {
    return (fromTo.first until fromTo.second).sumBy {
        this[it].totInt().shl((it - fromTo.first) * 8)
    }
}

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

/**
 * 计算 Byte数组校验和
 */
fun ByteArray.checksum(fromTo: Pair<Int, Int> = Pair(0, this.size)): Int {
    var cs = 0
    if (fromTo.first > fromTo.second || fromTo.second > size) {
        throw Throwable("参数错误")
    }
    (fromTo.first until fromTo.second).forEach {
        cs += this[it].toInt()
    }
    return cs.and(0xff)
}

/**
 * ByteArray逆序
 */
fun ByteArray.reverse(): ByteArray {
    val tmp = ByteArray(size)
    for (i in indices) {
        tmp[size - 1 - i] = this[i]
    }
    return tmp
}

/*Byte转Hex字符串*/
fun Byte.toHex(): String {
    return String.format("%02X", this.totInt())
}

/**
 * 将ip转换为16进制String
 * @param ipString
 * @return
 */
fun String.ipToHex(): String {
    val ip = this.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val sb = StringBuffer()
    for (str in ip) {
        val _ip = StringBuffer(Integer.toHexString(Integer.parseInt(str)))
        var strLen = _ip.length
        val strLength = 2
        if (strLen < strLength) {
            while (strLen < strLength) {
                _ip.insert(0, "0")
                strLen = _ip.length
            }
        }
        sb.append(_ip)
    }
    return sb.toString()
}

/**
 * hex字符串转位ByteArray
 */
fun String.toHexByteArray(): ByteArray {
    val hexString = this.trim().replace(" ", "")
    val bytes = ByteArray((hexString.length + 1) / 2)
    var i = hexString.length
    while (i > 0) {
        bytes[(i - 1) / 2] = Integer.parseInt(
                hexString.substring(if (i - 2 >= 0) i - 2 else i - 1, i),
                16
        ).toByte()
        i -= 2
    }
    return bytes
}

fun Int.toByteArray(length: Int): ByteArray {
    return ByteArray(length) {
        shr((length - 1 - it) * 8).and(0xFF).toByte()
    }
}

fun Long.toByteArray(length: Int): ByteArray {
    return ByteArray(length) {
        shr(it * 8).and(0xFF).toByte()
    }
}

/**
 * 直接转换为String
 */
fun ByteArray.rangeToString(fromTo: Pair<Int, Int>, charset: Charset = Charsets.UTF_8): String {
    return copyOfRange(fromTo.first, fromTo.second).map {
        if (it.totInt() == 0x00 || it.totInt() == 0xFF) 0x20 else it
    }.toByteArray().toString(charset)
}

fun getCurrentTime(): String {
    val pattern = "yy-MM-dd HH:mm:ss"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
}

fun String.getCurrentTime(): String {
    return SimpleDateFormat(this, Locale.getDefault()).format(Date())
}

fun getCurrentTimeToHex(): ByteArray {
    val pattern = "yyyyMMddHHmmss"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date()).toHexByteArray()
}

fun String.getCurrentTimeToHex(): ByteArray {
    return SimpleDateFormat(this, Locale.getDefault()).format(Date()).toHexByteArray()
}

fun getTimeTickTime(): ByteArray {
    val calendar = Calendar.getInstance()
    // 获取年yyyy
    val year = calendar.get(Calendar.YEAR) % 100
    // 获取月，这里需要需要月份的范围为0~11，因此获取月份的时候需要+1才是当前月份值
    val month = calendar.get(Calendar.MONTH) + 1
    // 获取日
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    // 获取时
    val hour = calendar.get(Calendar.HOUR_OF_DAY) // 24小时表示
    // 获取分
    val minute = calendar.get(Calendar.MINUTE)
    // 获取秒
    val second = calendar.get(Calendar.SECOND)
    // 星期，英语国家星期从星期日开始计算
    val weekday = calendar.get(Calendar.DAY_OF_WEEK).run {
        if (this == 1) {
            7
        } else {
            this - 1
        }
    }
    return String.format("%02d%02d%02d%02d%02d%02d%02d", year, month, day, weekday, hour, minute, second).toHexByteArray()
}