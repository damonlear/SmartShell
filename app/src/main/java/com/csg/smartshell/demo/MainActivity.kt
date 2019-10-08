package com.csg.smartshell.demo

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.csg.smartshell.demo.ui.bluetooth.DeviceListActivity
import com.csg.smartshell.demo.ui.main.MainFragment
import com.csg.smartshell.demo.ui.view.toast
import com.smartshell.listener.RFIDListener
import csg.device.utils.SmartShellBT
import csg.device.utils.SmartShellRFID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory


class MainActivity : AppCompatActivity() {

    // Local Bluetooth adapter
    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show()
            finish()
            return
        }





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


        SmartShellRFID.get().init()
                SmartShellRFID.get().listener(object : RFIDListener{
                    override fun onResult(p0: Int, p1: String?, p2: MutableList<String>?) {
                        logger.error("{}","p0：${p0} : p1：${p1} : p2：${p2}")
                    }
                })

    }

    private val REQUEST_CONNECT_DEVICE_SECURE = 1
    private val REQUEST_ENABLE_BT = 2


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CONNECT_DEVICE_SECURE -> if (resultCode == Activity.RESULT_OK) {
                val address = data?.extras!!
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)

                    logger.error("{}","init:${address}")

                GlobalScope.launch {
                    SmartShellRFID.get().apply {
                        macAddress = address
                        mContext = this@MainActivity
                    }.init()
                    logger.error("{}", "init:${address}")
                }

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

    companion object {
        val logger = LoggerFactory.getLogger(MainActivity::class.java)
    }
}
