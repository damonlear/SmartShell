package csg.device.utils

import com.smartshell.api.SmartshellPowerAPI
import com.smartshell.infrared.AndroidApplication
import com.smartshell.listener.CallbackListener
import com.smartshell.listener.ElectricListener
import com.smartshell.listener.QRBarCodeListener
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.CountDownLatch

class SmartShellDriver private constructor() {
    companion object {
        val instance: SmartShellDriver by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SmartShellDriver()
        }
    }

    private lateinit var mSmartshellPowerAPI: SmartshellPowerAPI

    private var connectionStatus = false
    private val connectionLock = CountDownLatch(1)
    private val connectionListener = object : CallbackListener {
        override fun callback(p0: Boolean, p1: String?) {
            connectionStatus = p0
            connectionLock.countDown()
            if (p0) {
                initAllListener()
            }
        }
    }

    private var dlt645Array: ByteArray = ByteArray(0)
    private val dlt645Lock = CountDownLatch(1)
    private var psamArray: ByteArray = ByteArray(0)
    private val psamLock = CountDownLatch(1)
    private var qrCode = ""
    private val qrCodeLock = CountDownLatch(1)

    //控制功能按键
    var deviceMode: Int = -1

    init {
        mSmartshellPowerAPI = SmartshellPowerAPI.getInstance(AndroidApplication.get())
    }

    fun initAllListener() {
        mSmartshellPowerAPI.setElectricListener(object : ElectricListener {
            //安全数据交互模块
            override fun onEncryptResult(p0: Int, p1: ByteArray?) {
                psamArray = p1!!
                psamLock.countDown()
            }

            //红外返回
            override fun onPowerResult(p0: Int, p1: ByteArray?) {
                dlt645Array = p1!!
                dlt645Lock.countDown()
            }
        })
        mSmartshellPowerAPI.setQRBarCodeListener(object : QRBarCodeListener {
            override fun onResult(p0: Int, p1: String?, p2: MutableList<String>?) {
                qrCode = p2!!.get(0)
                qrCodeLock.countDown()
            }
        })
    }

    fun init(macAddress: String?): Boolean {
        mSmartshellPowerAPI.onStart(macAddress, connectionListener)
        connectionLock.await()
        return connectionStatus
    }

    fun deInit() {
        mSmartshellPowerAPI.onStop()
        mSmartshellPowerAPI.disconnect()
    }

    suspend fun sendInfrared(byteArray: ByteArray): ByteArray {
        if (deviceMode != 3) {
            mSmartshellPowerAPI.switchDeviceMode(3)
            Thread.sleep(2000)
            deviceMode = 3
        }
        mSmartshellPowerAPI.sendCommandOnInfraredRFIDMode(0)
        Thread.sleep(1000)
        mSmartshellPowerAPI.sendCommand(byteArray)
        dlt645Lock.await()
        return dlt645Array
    }


    suspend fun sendLaserInfrared(byteArray: ByteArray): ByteArray {
        if (deviceMode != 3) {
            mSmartshellPowerAPI.switchDeviceMode(3)
            Thread.sleep(1000)
            deviceMode = 3
        }
        mSmartshellPowerAPI.sendCommandOnInfraredRFIDMode(1)
        Thread.sleep(1000)
        mSmartshellPowerAPI.sendCommand(byteArray)
        dlt645Lock.await()

        mSmartshellPowerAPI.sendCommandOnInfraredRFIDMode(0)
        Thread.sleep(1000)
        return dlt645Array
    }


    fun sendQRCode(): String {
        if (deviceMode != 2) {
            mSmartshellPowerAPI.switchDeviceMode(2)
            Thread.sleep(3000)
            deviceMode = 2
            mSmartshellPowerAPI.sendCmdOnStartScanner()
            Thread.sleep(1500)
        }
        mSmartshellPowerAPI.sendCmdOnStartScanner()
        qrCodeLock.await()
        return qrCode
    }

    suspend fun sendPsam(byteArray: ByteArray): ByteArray {
        if (deviceMode != 3) {
            mSmartshellPowerAPI.switchDeviceMode(3)
            Thread.sleep(1000)
            deviceMode = 3
        }
        mSmartshellPowerAPI.sendCommand(byteArray)
        psamLock.await()
        return psamArray
    }
}