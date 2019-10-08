package csg.device.utils

import android.content.Context
import com.csg.smartshell.demo.AndroidApplication
import com.smartshell.listener.CallbackListener
import com.smartshell.listener.RFIDListener
import java.util.concurrent.CountDownLatch

class SmartShellRFID private constructor() : SmartShellBT() {
    companion object {
        private var instance: SmartShellRFID? = null
            get() {
                if (field == null) {
                    field = SmartShellRFID()
                }
                return field
            }

        fun get(): SmartShellRFID {
            //细心的小伙伴肯定发现了，这里不用getInstance作为为方法名，是因为在伴生对象声明时，内部已有getInstance方法，所以只能取其他名字
            return instance!!
        }
    }


    override var macAddress: String?
        get() = ""
        set(value) {}


    override var mContext: Context
        get() = AndroidApplication.get()
        set(value) {}

    override val type: Int
        get() = -1

    override var state: Status
        get() = Status.CLOSE
        set(value) {}

    lateinit var smartshellRFIDAPI: com.smartshell.api.SmartshellRFIDAPI

    init {
        smartshellRFIDAPI = com.smartshell.api.SmartshellRFIDAPI.getInstance(mContext)
    }

    override fun init() {
        val lock = CountDownLatch(1)
        logger.error("{}", macAddress)
        smartshellRFIDAPI.onStart("98:D3:31:F3:15:C5", object : CallbackListener {
            override fun callback(isConnection: Boolean, result: String?) {
                state = if (isConnection) {
                    Status.OPEN
                } else {
                    Status.CLOSE
                }
                logger.error("{},{}", isConnection, result)
                lock.countDown()
            }
        })
        smartshellRFIDAPI.onStop()
        lock.await()
    }

    override fun deinit() {
        smartshellRFIDAPI.close()
        state = Status.CLOSE
    }

    override fun write(bytes: ByteArray) {
        if (state == Status.CLOSE) {
            return
        }
    }

    override fun read(buff: ByteArray): Int {
        val lock = CountDownLatch(1)
        smartshellRFIDAPI.setRFIDListener(object : RFIDListener {
            override fun onResult(p0: Int, p1: String?, p2: MutableList<String>?) {
                logger.error("{}", "p0：${p0} : p1：${p1} : p2：${p2}")
                lock.countDown()
            }
        })
        lock.await()
        return 0
    }

    fun listener(l: RFIDListener) {
        smartshellRFIDAPI.setRFIDListener(l)
    }
}