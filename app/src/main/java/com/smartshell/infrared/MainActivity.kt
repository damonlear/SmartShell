package com.smartshell.infrared

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartshell.api.SmartshellPowerAPI
import com.smartshell.infrared.bean.PsamInfo
import com.smartshell.infrared.ui.bluetooth.DeviceListActivity
import com.smartshell.infrared.ui.view.toast
import com.smartshell.listener.CallbackListener
import com.smartshell.listener.ElectricListener
import com.smartshell.listener.PowerListener
import com.smartshell.listener.QRBarCodeListener
import csg.device.utils.SmartShellBT
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import android.text.TextUtils


class MainActivity : AppCompatActivity() {

    // Local Bluetooth adapter
    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        initView()
    }

    override fun onStart() {
        super.onStart()
        //如果蓝牙未开，启动本机蓝牙
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
            // Otherwise, setup the chat session
        }
        if (getMacAddress().isNullOrBlank()) {
            getBluetooth()
        } else {
            initSmartShell()
        }
    }

    override fun onDestroy() {
        smartshellPowerAPI.onStop()
        smartshellPowerAPI.disconnect()
        super.onDestroy()
    }

    private fun initView() {
        setbluetooth.setOnClickListener {
            getBluetooth()
        }
        ir.setOnClickListener {
            if (!bltStatus) {
                toast("请先连接蓝牙")
                return@setOnClickListener
            }
            setResult("")
            testIr()
        }
        jgir.setOnClickListener {
            if (!bltStatus) {
                toast("请先连接蓝牙")
                return@setOnClickListener
            }
            setResult("")
            testjgIr()
        }
        scan.setOnClickListener {
            if (!bltStatus) {
                toast("请先连接蓝牙")
                return@setOnClickListener
            }
            setResult("")
            testScan()
        }
        psam.setOnClickListener {
            if (!bltStatus) {
                toast("请先连接蓝牙")
                return@setOnClickListener
            }
            setResult("")
            testPsam()
        }

    }

    private val REQUEST_CONNECT_DEVICE_SECURE = 1
    private val REQUEST_ENABLE_BT = 2


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CONNECT_DEVICE_SECURE -> if (resultCode == Activity.RESULT_OK) {
                val address = data?.extras!!
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
                logger.error("---{}", "init:${address}")
                saveMacAddress(address)
                initSmartShell()
            }

            REQUEST_ENABLE_BT -> if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this@MainActivity, "蓝牙可用", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "蓝牙不可用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMacAddress(): String? {
        val sharedata = getSharedPreferences("data", 0)
        return sharedata.getString(SmartShellBT.SPF_MAC_ADDRESS, null)

    }

    private fun saveMacAddress(address: String?) {
        val sharedata = getSharedPreferences("data", 0).edit()
        sharedata.putString(SmartShellBT.SPF_MAC_ADDRESS, address)
        sharedata.apply()
    }

    fun getBluetooth() {
        val serverIntent = Intent(this@MainActivity, DeviceListActivity::class.java)
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE)
    }

    val smartshellPowerAPI = SmartshellPowerAPI.getInstance(this)
    var bltStatus: Boolean = false
    //控制红外还是激光红外
    var irStatus: Int = -1
    //控制功能按键
    var deviceMode: Int = -1

    fun initSmartShell() {
        smartshellPowerAPI.onStart(getMacAddress(), object : CallbackListener {
            override fun callback(p0: Boolean, p1: String?) {
                toast("p0:${p0} , p1:${p1}")
                bltStatus = p0
                if (p0) {
                    smartshellPowerAPI.sendCommandOnOpenKey()
                }
                setThisTitle()
            }
        })
        initAllListener()
    }

    fun initAllListener() {
        smartshellPowerAPI.setElectricListener(object : ElectricListener {
            //psam
            override fun onEncryptResult(p0: Int, p1: ByteArray?) {
                setResult("安全数据交互模块接收：${p1?.toHexString()}")
                try {
                    val psamInfo = PsamInfo.parse(p1!!)
                    appendResult(
                        "\r\n" +
                                "安全数据加密模块信息\r\n" +
                                "${if (PsamInfo.isPublicKey(psamInfo)) {
                                    "未发行芯片"
                                } else {
                                    "已发行芯片"
                                }}:\r\n" +
                                "芯片软件版本：${psamInfo.softwareVersion}\r\n" +
                                "芯片硬件版本：${psamInfo.hardwareVersion}\r\n" +
                                "C-ESAM序列号：${psamInfo.cesamSeq}\r\n" +
                                "Y-ESAM序列号：${psamInfo.yesamSeq}\r\n" +
                                "C-ESAM对称密钥密钥版本：${psamInfo.cesamKeyVersion}\n" +
                                "Y-ESAM对称密钥密钥版本：${psamInfo.yesamKeyVersion}\r\n"
                    )
                } catch (e: Throwable) {
                }
            }

            //红外
            override fun onPowerResult(p0: Int, p1: ByteArray?) {
                setResult("红外接收：${p1?.toHexString()}")
            }
        })
        smartshellPowerAPI.setPowerListener(object : PowerListener{
            override fun onSuccess(s: String?) {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        if (!TextUtils.isEmpty(s)) {
                            // 电表数据
                            var newstr = s!!
                            newstr = newstr.replace("add:", "\n 表号");
                            newstr = newstr.replace("T0:", "\n 当前正向有功总(kWh)");
                            newstr = newstr.replace("T1:", "\n 当前正向有功尖(kWh)");
                            newstr = newstr.replace("T2:", "\n 当前正向有功峰(kWh)");
                            newstr = newstr.replace("T3:", "\n 当前正向有功平(kWh)");
                            newstr = newstr.replace("T4:", "\n 当前正向有功谷(kWh)");
                            newstr = newstr.replace("T5:", "\n 当前正向无功总(kvarh)");
                            newstr = newstr.replace("T8:", "\n A相电压(v)");
                            newstr = newstr.replace("T9:", "\n B相电压(v)");
                            newstr = newstr.replace("TA:", "\n C相电压(v)");
                            newstr = newstr.replace("TB:", "\n A相电流(v)");
                            newstr = newstr.replace("TC:", "\n B相电流(v)");
                            newstr = newstr.replace("TD:", "\n C相电流(v)");
                            newstr = newstr.replace("R0:", "\n 当前反向有功总(kWh)");
                            newstr = newstr.replace("R1:", "\n 当前反向有功尖(kWh)");
                            newstr = newstr.replace("R2:", "\n 当前反向有功峰(kWh)");
                            newstr = newstr.replace("R3:", "\n 当前反向有功平(kWh)");
                            newstr = newstr.replace("R4:", "\n 当前反向有功谷(kWh)");
                            newstr = newstr.replace("R5:", "\n 当前反向无功总(kvarh)");
                            newstr = newstr.replace("X0:", "\n 最近冻结正向有功总(kWh)");
                            newstr = newstr.replace("X1:", "\n 最近冻结正向有功尖(kWh)");
                            newstr = newstr.replace("X2:", "\n 最近冻结正向有功峰(kWh)");
                            newstr = newstr.replace("X3:", "\n 最近冻结正向有功平(kWh)");
                            newstr = newstr.replace("X4:", "\n 最近冻结正向有功谷(kWh)");
                            appendResult("数据解析：\n$newstr")
                        }
                    }
                });
            }

            override fun onFail(p0: Int, p1: String?) {
                runOnUiThread {
                    if (!p1.isNullOrBlank()){
                        setResult("\n${p0}:${p1}")
                    }
                }
            }
        })
        //扫码
        smartshellPowerAPI.setQRBarCodeListener(object : QRBarCodeListener {
            override fun onResult(p0: Int, p1: String?, p2: MutableList<String>?) {
                setResult("扫码接收：${p2?.get(0)}")
            }
        })
        smartshellPowerAPI.sendCommandOnOpenKey()
    }

    fun testIr() {
        GlobalScope.launch(Dispatchers.IO) {
            val ir = byteArrayOf(
                0x68,
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0x68,
                0x11,
                0x04,
                0x33,
                0x33,
                0x34,
                0x33,
                0xAE.toByte(),
                0x16
            )
            val ir_s = "68aaaaaaaaaaaa68110433333433ae16"
            if (deviceMode != 3) {
                smartshellPowerAPI.switchDeviceMode(3)
                Thread.sleep(2000)
                deviceMode = 3
            }
            smartshellPowerAPI.sendCommandOnInfraredRFIDMode(0)
            Thread.sleep(1000)
            smartshellPowerAPI.sendCommand(ir_s)
            deviceMode = 3
        }
    }

    fun testjgIr() {
        GlobalScope.launch(Dispatchers.IO) {
            val ir = byteArrayOf(
                0x68,
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0xAA.toByte(),
                0x68,
                0x11,
                0x04,
                0x33,
                0x33,
                0x34,
                0x33,
                0xAE.toByte(),
                0x16
            )
            val ir_s = "68aaaaaaaaaaaa68110433333433ae16"
            if (deviceMode != 3) {
                smartshellPowerAPI.switchDeviceMode(3)
                Thread.sleep(1000)
                deviceMode = 3
            }
            smartshellPowerAPI.sendCommandOnInfraredRFIDMode(1)
            Thread.sleep(1000)
            smartshellPowerAPI.sendCommand(ir_s)
            Thread.sleep(1500)
            smartshellPowerAPI.sendCommandOnInfraredRFIDMode(0)
        }
    }

    fun testScan() {
        GlobalScope.launch(Dispatchers.IO) {
            if (deviceMode != 2) {
                smartshellPowerAPI.switchDeviceMode(2)
                Thread.sleep(2000)
                deviceMode = 2
                smartshellPowerAPI.sendCmdOnStartScanner()
                Thread.sleep(1000)
            }
            smartshellPowerAPI.sendCmdOnStartScanner()
            Thread.sleep(1000)
        }
    }

    fun testPsam() {
        GlobalScope.launch(Dispatchers.IO) {
            val psam = byteArrayOf(0x55, 0x00, 0xA0.toByte(), 0x01, 0x00, 0x00, 0x00, 0x5E)
            val psam_s = "5500A0010000005E"
            if (deviceMode != 3) {
                smartshellPowerAPI.switchDeviceMode(3)
                Thread.sleep(1000)
                deviceMode = 3
            }
            smartshellPowerAPI.sendCommand(psam_s)
            Thread.sleep(1000)
        }
    }

    fun setThisTitle() {
        GlobalScope.launch(Dispatchers.Main) {
            title = "[${getMacAddress()}]:${if (bltStatus) "已连接" else "未连接"}"
        }
    }

    fun setResult(s: String) {
        GlobalScope.launch(Dispatchers.Main) {
            tvResult.text = s
        }
    }

    fun appendResult(s: String) {
        GlobalScope.launch(Dispatchers.Main) {
            tvResult.append(s)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(MainActivity::class.java)
    }
}
