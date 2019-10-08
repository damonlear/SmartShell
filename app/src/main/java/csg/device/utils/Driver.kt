package csg.device.utils

import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by hotsxu on 2018/3/23.
 *
 * Driver
 */
abstract class Driver {

    /*设备类型*/
    abstract val type: Int
    /*设备广泰*/
    abstract var state: Status

    abstract  fun init()

    abstract  fun deinit()

    abstract  fun write(bytes: ByteArray)

    abstract  fun read(buff: ByteArray): Int

    /**
     * 状态
     */
    enum class Status {
        CLOSE,
        OPEN,
        USE,
        WRONG,
    }

    companion object {
        val logger = LoggerFactory.getLogger(Driver::class.java)
    }
}