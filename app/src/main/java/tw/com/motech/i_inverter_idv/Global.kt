package tw.com.motech.i_inverter_idv

import java.sql.Connection
import java.sql.DriverManager


val BaseUrl : String = "https://i-inverter.motech.com.tw/vUserApisvr/api/values"
var LoginSP : String = "USER_LOGIN"
var ServerUrl: String = "xx"
var UserName : String = "XX"
var PassWord : String = "xxx"
var sSiteNo_GLB: String = "MOT001"
var sSite_Name_GLB: String = "茂迪五廠"
var sZoneNo_GLB: String = "F5b1zone"
var ServerConn : Connection? = null


data class SiteResult(
    val sSiteType: String,
    val nSort: Int,
    val sSiteNo: String,
    val sSite_Name: String,
    val nSHI: Int,
    val nDMY: Double,
    val nPR: Double
)

data class ZoneResult(
    val sZoneNo: String,
    val sMemo: String
)

data class SiteData(
    val sDataKey: String,
    val nEa: Double,
    val nHi: Float,
    val nTmp: Float
)

data class LastSiteData(
    val sDataKey: String,
    val sSiteNo: String,
    val nEa: Double,
    val nHi: Float,
    val nTmp: Float,
    val nTEa: Double,
    val nEaMax: Double
)

data class InverterResult(
    val sNo: String,
    val sDataKey: String,
    val sSNID: String,
    val nRS485ID: Int,
    val nEa: Double,
    val sErrCode: String,
    val ConChk: Int,
    val dCreat_Time: String,
    val sInvModel: String
)

data class InverterChkList(
    val nRS485ID: Int,
    val sSNID: String,
    val sInvModel: String
)

data class InvStringData(
    var sDataKey: String,
    val nRS485ID: Int,
    val sSNID: String,
    val nEa: Float?,
    val nOVol: Float?,
    val nOCur: Float?,
    val nPpv: Float?,
    val nVpv_A: Float?,
    val nVpv_B: Float?,
    val nVpv_C: Float?,
    val nVpv_D: Float?,
    val nVpv_E: Float?,
    val nVpv_F: Float?,
    val nVpv_G: Float?,
    val nVpv_H: Float?,
    val nVpv_I: Float?,
    val nVpv_J: Float?,
    val nVpv_K: Float?,
    val nVpv_L: Float?,
    val nIpv_A: Float?,
    val nIpv_B: Float?,
    val nIpv_C: Float?,
    val nIpv_D: Float?,
    val nIpv_E: Float?,
    val nIpv_F: Float?,
    val nIpv_G: Float?,
    val nIpv_H: Float?,
    val nIpv_I: Float?,
    val nIpv_J: Float?,
    val nIpv_K: Float?,
    val nIpv_L: Float?
)

data class ParameterChkList(
    val sType: String,
    val sName: String,
    val sName2: String
)