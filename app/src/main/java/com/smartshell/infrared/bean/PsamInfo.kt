package com.smartshell.infrared.bean

import com.pemt.detection.extensions.rangeToString
import com.pemt.detection.extensions.sumOf
import com.pemt.detection.extensions.toHex
import com.pemt.detection.extensions.totInt
import java.io.Serializable

//已发行
//55 90 00 00 B3 00 30 2E 32 35 32 2E 30 30 00 03 91 22 00 00 03 76 02 02 02 02 02 02 02 02 02 02 02 02 00 00 00 00 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 7A 63 74 65 73 74 30 31 0D E7 BD 91 E7 9C 81 E5 85 AC E5 8F B8 0D 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 00 03 92 01 00 00 03 40 02 02 02 02 02 02 02 02 02 02 02 02 02 02 02 00 00 00 0F 00 00 00 0F 00 00 00 0F 00 00 00 0F 00 00 00 00 76 0F 00 95
//未发行
//55 90 00 00 B3 00 30 2E 32 35 32 2E 30 30 00 03 91 03 00 00 13 C7 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 92 03 00 00 14 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 0F 00 00 00 0F 00 00 00 0F 00 00 00 00 00 0F 0F 2B
//55 90 00 00 B3 00 30 2E 32 35 32 2E 30 30 00 03 91 09 00 00 04 B6 01 01 01 01 01 01 01 01 01 01 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 92 08 00 00 03 6C 01 01 01 01 01 01 00 01 01 00 00 01 01 01 01 00 00 00 0F 00 00 00 0F 00 00 00 0F 00 00 00 0F 00 00 00 00 00 0F 0F 06
data class PsamInfo(
    var status: Int,//安全数据交互模块状态字---[0,1)
    var softwareVersion: String,//软件版本号---[1,5)
    var hardwareVersion: String,//硬件版本号---[5,9)
    var cesamSeq: String,//C-ESAM序列号---[9,17)
    var cesamKeyVersion: String,//C-ESAM对称密钥密钥版本---[17,33)
    var operatorCode: String,//操作者代码---[33,37)
    var permission: String,//权限---[37,38)
    var operatorInfo: String,//操作者信息---[38,133)
    var yesamSeq: String,//Y-ESAM序列号---[133,141)
    var yesamKeyVersion: String,//Y-ESAM对称密钥密钥版本---[141,157)
    var irAuthenticationCount1: Int,//红外认证剩余次数1---[157,161)
    var irAuthenticationCount2: Int,//红外认证剩余次数2---[161,165)
    var irAuthenticationCount3: Int,//红外认证剩余次数3---[165,169)
    var irAuthenticationCount4: Int,//红外认证剩余次数4---[169,173)
    var outLineCount: Int,//离线计数器次数---[173,177)
    var pinMaxCount: Int,//PIN码校验允许最大次数---[177,178)
    var pinResidueCount: Int//PIN码剩余次数---[178,179)
) :Serializable {

    companion object {
        fun parse(bytes: ByteArray): PsamInfo {
            //5开始
            if (bytes.size < 185) {
                throw Throwable("数据长度不足")
            }
            return PsamInfo(
                bytes[5].totInt(),
                bytes.rangeToString(6 to 10),
                bytes.rangeToString(10 to 14),
                bytes.toHex(14 to 22),
                bytes.toHex(22 to 38),
                bytes.toHex(38 to 42),
                bytes.toHex(42 to 43),
                bytes.copyOfRange(43, 138).parseOperatorInfo(),
                bytes.toHex(138 to 146),
                bytes.toHex(146 to 162),
                bytes.sumOf(162 to 166),
                bytes.sumOf(166 to 170),
                bytes.sumOf(170 to 174),
                bytes.sumOf(174 to 178),
                bytes.sumOf(178 to 182),
                bytes.sumOf(182 to 183),
                bytes.sumOf(183 to 184)
            )
        }
        // FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7A637465737430310DE7BD91E79C81E585ACE58FB80D31313131313131313131313131313131

        // 7A637465737430310D
        // E7BD91E79C81E585ACE58FB80D
        // 31313131313131313131313131313131

        // zctest01
        // 网省公司
        // 1111111111111111
        fun ByteArray.parseOperatorInfo(): String {
            if (this.size == 95) {
                val srcPos = this.indexOfLast { it == 0xFF.toByte() || it == 0X00.toByte() } + 1
                val des = ByteArray(95 - srcPos)
                System.arraycopy(this, srcPos, des, 0, des.size)
                val list = des.rangeToString(0 to des.size).split(Regex("\\s+"))

                if (list.isNotEmpty()) {
                    return list[0]
                }
            } else {

            }
            return ""
        }


        fun isPublicKey(psamInfo: PsamInfo): Boolean {
            //测试芯片未发行
            val cesam_debug_publicKey =
                psamInfo.cesamKeyVersion == "01010101010101010101010100000000"
            val yesam_debug_publicKey =
                psamInfo.yesamKeyVersion == "01010101010100010100000101010100"
            //正式芯片未发行
            val yesam_release_publicKey =
                psamInfo.yesamKeyVersion == "00000000000000000000000000000000"
            val cesam_release_publicKey =
                psamInfo.cesamKeyVersion == "00000000000000000000000000000000"

            return (cesam_debug_publicKey && yesam_debug_publicKey) || (yesam_release_publicKey && cesam_release_publicKey)
        }
    }
}