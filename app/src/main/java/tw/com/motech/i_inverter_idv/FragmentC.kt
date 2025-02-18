package tw.com.motech.i_inverter_idv

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.androidbuts.multispinnerfilter.KeyPairBoolData
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AALabels
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AATitle
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAYAxis
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_a.view.*
import kotlinx.android.synthetic.main.fragment_c.*
import kotlinx.android.synthetic.main.fragment_c.view.*
import okhttp3.*
import java.io.IOException
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class FragmentC : Fragment() {
    private lateinit var v:View
    //private lateinit var listInverterChkList: List<InverterChkList>
    private lateinit var listInverterChkList: MutableList<InverterChkList>
    private lateinit var listParameterChkList: MutableList<ParameterChkList>
    //private lateinit var listSiteData: List<SiteData>
    private lateinit var listSiteData: MutableList<SiteData>
    //private lateinit var listInvStringData: List<InvStringData>
    private lateinit var listInvStringData: MutableList<InvStringData>
    private lateinit var mapInvStringData : MutableMap<String, MutableList<InvStringData>> // key是SNID, value是InvStringData的陣列

    private var selectedInv : String = ""
    private var selectedPara : String = ""
    private lateinit var listSelectedPara : MutableList<String>
    private lateinit var listInv : MutableList<String>
    private lateinit var listPara : MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_c, container, false)

        mapInvStringData = mutableMapOf()
        listSelectedPara = mutableListOf()
        listInv = mutableListOf()
        listPara = mutableListOf()

        initParaList()
        initInvMultiSpinner()
        initParaMultiSpinner()

        v.invMultiSpinner.setOnClickListener {
            val checkBoxListView = CheckBoxListView(v, listInv, ::handleInvSelectedOptions)
            checkBoxListView.showCheckBoxListDialog()
        }
        v.paraMultiSpinner.setOnClickListener {
            val checkBoxListView = CheckBoxListView(v, listPara, ::handleParaSelectedOptions)
            checkBoxListView.showCheckBoxListDialog()
        }

        // 日期
        v.btndate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(v.context, { _, year, month, day ->
                run {
                    val d = setDateFormat(year, month, day)
                    v.txtdate.text = d
                }
            }, year, month, day).show()
        }

        // 確認
        v.btnQuery.setOnClickListener{
            var valid : Boolean = true

            if (v.txtdate.text.toString().length == 0) {
                Toast.makeText(v.context, "請選擇日期", Toast.LENGTH_SHORT).show()
                valid = false
            }

            // 除了nHi和nTmp以外，如果有勾以下參數要檢查inverter有沒有選
            if (selectedPara.contains("nEa") ||
                selectedPara.contains("nPpv") ||
                selectedPara.contains("nOVol") ||
                selectedPara.contains("nVpv") ||
                selectedPara.contains("nOCur") ||
                selectedPara.contains("nIpv")   ) {
                if (selectedInv.length == 0) {
                    Toast.makeText(v.context, "請至少選擇一台inverter", Toast.LENGTH_SHORT).show()
                    valid = false
                }
            }

            if (valid) {
                getSiteData(v.txtdate.text.toString())
            }
        }

        return v
    }

    private fun initParaList() {
        listParameterChkList = mutableListOf<ParameterChkList>()
        listParameterChkList.add(ParameterChkList("交流側", "nEa", "發電功率"))
        listParameterChkList.add(ParameterChkList("交流側", "nOVol", "電壓"))
        listParameterChkList.add(ParameterChkList("交流側", "nOCur", "電流"))

        listParameterChkList.add(ParameterChkList("直流側", "nPpv", "輸入功率"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_A", "輸入電壓-A串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_B", "輸入電壓-B串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_C", "輸入電壓-C串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_D", "輸入電壓-D串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_E", "輸入電壓-E串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_F", "輸入電壓-F串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_G", "輸入電壓-G串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_H", "輸入電壓-H串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_I", "輸入電壓-I串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_J", "輸入電壓-J串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_K", "輸入電壓-K串"))
        listParameterChkList.add(ParameterChkList("直流側", "nVpv_L", "輸入電壓-L串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_A", "輸入電流-A串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_B", "輸入電流-B串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_C", "輸入電流-C串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_D", "輸入電流-D串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_E", "輸入電流-E串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_F", "輸入電流-F串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_G", "輸入電流-G串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_H", "輸入電流-H串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_I", "輸入電流-I串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_J", "輸入電流-J串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_K", "輸入電流-K串"))
        listParameterChkList.add(ParameterChkList("直流側", "nIpv_L", "輸入電流-L串"))

        listParameterChkList.add(ParameterChkList("感測器", "nHi", "日照計"))
        listParameterChkList.add(ParameterChkList("感測器", "nTmp", "溫度計"))
    }

    private fun initInvMultiSpinner() {
       /*
        v.invMultiSpinner.isSearchEnabled = true
        v.invMultiSpinner.isShowSelectAllButton = true
        v.invMultiSpinner.setSearchHint("搜尋 inverter")
        v.invMultiSpinner.setEmptyTitle("找不到 inverter!")
        v.invMultiSpinner.setClearText("關閉")
        */

        getInverterList()
    }

    private fun initParaMultiSpinner() {
        /*
        v.paraMultiSpinner.isSearchEnabled = true
        v.paraMultiSpinner.setSearchHint("搜尋 parameter")
        v.paraMultiSpinner.setEmptyTitle("找不到 parameter!")
        v.paraMultiSpinner.setClearText("關閉")
         */
        getParameterList()
    }

    private fun getInverterList() {
        listInverterChkList = mutableListOf<InverterChkList>()
        thread {
            var ResultMsg = ""
            Class.forName("org.postgresql.Driver")
            try {
                //val conn = DriverManager.getConnection("jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm?sslmode=verify-full","motechpvm","-QR-wAfQ6WRno4RGWTOX2g")

                if (ServerConn == null) {
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                if ( !ServerConn!!.isValid(0)) {
                    println("getInverterList:" + ServerConn!!.isValid(0))
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                println("getInverterList:" + ServerConn!!.isValid(0))
                Thread.sleep(1000) // cockroach request slow
                val strSQL = "SELECT modbus_addr as \"nRS485ID\", sn_name as \"sSNID\", " +
                        "(brand_name || '_' || model_type) as \"sInvModel\" " +
                        "FROM s_inverter_setting " +
                        "WHERE online = 'Y' " +
                        "ORDER BY inverter_id ASC, modbus_addr ASC, sn_name ASC, " +
                        "(brand_name || '_' || model_type) ASC"

                val statement = ServerConn!!.prepareStatement(strSQL)
                val rs = statement.executeQuery()
                listInverterChkList.clear()
                while (rs.next()) {
                    val nRS485ID = rs.getInt("nRS485ID")
                    val sSNID = rs.getString("sSNID")
                    val sInvModel = rs.getString("sInvModel")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sDataKey,$nEa,$nHi,$nTmp \n"
                    listInverterChkList.add(InverterChkList(nRS485ID,sSNID,sInvModel))
                }
                //inverterresults = inverterresults_tmp.toList()
                //ServerConn.close()
                getActivity()?.runOnUiThread {
                    if(listInverterChkList.count() > 0) {
                        listInv.clear()
                        for (l in listInverterChkList) {
                            //val h = KeyPairBoolData()
                            //h.id = (l.nRS485ID).toLong()
                            //h.name = l.sSNID + ":" + l.nRS485ID
                            //h.isSelected = false
                            listInv.add(l.sSNID + ":" + l.nRS485ID)
                        }
                    }
                }
            } catch (e: SQLException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "Failed to Connect", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLTimeoutException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "Connection timeout", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: ClassNotFoundException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "ClassNotFoundException", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            }
        } //end thread

        /*
        var funCode = "V01_MySolarToday09"
        var funValues = "'$sSiteNo_GLB';'$sZoneNo_GLB'"

        val formBody = FormBody.Builder()
            .add("FunCode", funCode)
            .add("FunValues", funValues)
            .build()

        val request = Request.Builder()
            .url(BaseUrl)
            .post(formBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val json = response.body!!.string() // 資料只能抓一次
                    listInverterChkList = Gson().fromJson(json, Array<InverterChkList>::class.java).toList()
                    listInv.clear()
                    for (l in listInverterChkList) {
                        //val h = KeyPairBoolData()
                        //h.id = (l.nRS485ID).toLong()
                        //h.name = l.sSNID + ":" + l.nRS485ID
                        //h.isSelected = false
                        listInv.add(l.sSNID + ":" + l.nRS485ID)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("查詢失敗", "$e")
            }
        })
        */
    }
    private fun handleInvSelectedOptions(selectedOptions: List<String>) {
        selectedInv = ""
        mapInvStringData = mutableMapOf()
        for (i in selectedOptions) {
            val inv = i.split(":")[0]
            // 組sql字串
            // AND C.sn_name in ('O1Y19804686WF','O1Y19804682WF','O1Y19804684WF')
            selectedInv = selectedInv + "'" + inv + "', "
            mapInvStringData[inv] = mutableListOf()

        }

        if (selectedInv.length > 2) {
            selectedInv = "AND C.sn_name in (" + selectedInv.substring(0, selectedInv.length - 2) + ")"
            println(selectedInv)
        }
    }

    private fun handleParaSelectedOptions(selectedOptions: List<String>) {
        selectedPara = ""
        listSelectedPara = mutableListOf()
        for (i in selectedOptions) {
            val p = listParameterChkList.filter {
                it.sName2 == i.split("_")[1]
            }
            if (p.isNotEmpty()) {
                // 組sql字串
                // " ,ROUND((B.nPac)/1000,2) as nEa ,B.nOVol ,B.nOCur ,ROUND((B.nPpv)/1000,2) as nPpv...
                if (p[0].sName == "nEa") {
                    selectedPara += "ROUND((A.pac_all/1000),2) as \"nEa\", "
                } else if (p[0].sName == "nOVol") {
                    selectedPara += "A.vac_all as \"nOVol\", "
                } else if (p[0].sName == "nOCur") {
                    selectedPara += "A.iac_all as \"nOCur\", "
                } else if (p[0].sName == "nPpv") {
                    selectedPara += "ROUND((A.ppv_all/1000),2) as \"nPpv\", "
                } else if (p[0].sName == "nHi") {
                    selectedPara += "B.sunshine as \"nHi\", "
                } else if (p[0].sName == "nTmp") {
                    selectedPara += "B.moduletemp as \"nTmp\", "
                } else {
                    selectedPara += "COALESCE(CAST(REGEXP_REPLACE(REGEXP_EXTRACT(A.detail_info, '<" + p[0].sName.removePrefix("n") + ">([^<]+)</" + p[0].sName.removePrefix("n") + ">'), ',.*', '') AS FLOAT), 0) AS \"" + p[0].sName + "\", "
                    //selectedPara = selectedPara + "B." + p[0].sName + ", "
                }
                listSelectedPara.add(p[0].sName)
            }
        }

        if (selectedPara.length > 2) {
            selectedPara = " ," + selectedPara.substring(0, selectedPara.length - 2)
            println(selectedPara)
        }

    }

    private fun getParameterList() {

        listPara.clear()
        for (i in listParameterChkList.indices) {
            /*
            val h = KeyPairBoolData()
            h.id = (i + 1).toLong()
            h.name = listParameterChkList[i].sType + "_" + listParameterChkList[i].sName2
            h.isSelected = false
             */
            listPara.add(listParameterChkList[i].sType + "_" + listParameterChkList[i].sName2)
        }
/*
        v.paraMultiSpinner.setItems(
            listArray0
        ) { items ->
            selectedPara = ""
            listSelectedPara = mutableListOf()
            for (i in items.indices) {
                if (items[i].isSelected) {
                    val p = listParameterChkList.filter {
                        it.sName2 == items[i].name.split("_")[1]
                    }
                    if (p.isNotEmpty()) {
                        // 組sql字串
                        // " ,ROUND((B.nPac)/1000,2) as nEa ,B.nOVol ,B.nOCur ,ROUND((B.nPpv)/1000,2) as nPpv...
                        if (p[0].sName == "nEa") {
                            selectedPara += "ROUND((B.nPac)/1000,2) as nEa, "
                        } else if (p[0].sName == "nPpv") {
                            selectedPara += "ROUND((B.nPpv)/1000,2) as nPpv, "
                        }  else  {
                            selectedPara = selectedPara + "B." + p[0].sName + ", "
                        }
                        listSelectedPara.add(p[0].sName)
                    }
                }
            }

            if (selectedPara.length > 2) {
                selectedPara = " ," + selectedPara.substring(0, selectedPara.length - 2)
                //println(selectedPara)
            }

        }

 */
    }

    private fun getSiteData(d: String) {
        listSiteData = mutableListOf<SiteData>()
        Thread(){
            var ResultMsg = ""
            Class.forName("org.postgresql.Driver")
            try {
                //val conn = DriverManager.getConnection("jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm?sslmode=verify-full","motechpvm","-QR-wAfQ6WRno4RGWTOX2g")

                if (ServerConn == null) {
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                if ( !ServerConn!!.isValid(0)) {
                    println("getSiteData:" + ServerConn!!.isValid(0))
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                println("getSiteData:" + ServerConn!!.isValid(0))
                Thread.sleep(1000) // cockroach request slow
                //val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                //currentDateAndTime = simpleDateFormat.format(Date())
                val dateStart: String = d + " 04:00"
                val dateEnd: String = d + " 19:59"

                val strSQL = "SELECT TO_CHAR (log_time, 'YYYYMMDDHH24MI') as \"sDataKey\", " +
                        "ROUND((pac_all/1000),2) as \"nEa\", sunshine as \"nHi\", moduletemp as \"nTmp\" " +
                        "FROM d_site_monitor_data " +
                        "WHERE log_time >= '$dateStart' AND log_time <= '$dateEnd' " +
                        "ORDER BY log_time ASC"

                val statement = ServerConn!!.prepareStatement(strSQL)
                val rs = statement.executeQuery()
                var i = 0
                listSiteData.clear()
                while (rs.next()) {
                    val sDataKey = rs.getString("sDataKey")
                    val nEa = rs.getDouble("nEa")
                    val nHi = rs.getFloat("nHi")
                    val nTmp = rs.getFloat("nTmp")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sDataKey,$nEa,$nHi,$nTmp \n"
                    i++
                    if ((i % 3) == 0) {  //因資料太細，每三筆顯示一次
                        listSiteData.add(SiteData(sDataKey,nEa,nHi,nTmp))
                    }
                }
                //ServerConn.close()
                if (selectedPara.length == 0 && selectedInv.length == 0) {
                    getActivity()?.runOnUiThread {
                        showAAChart()
                    }
                } else {
                    // 取得Site Data後，再去抓Inv Data
                    getInverterStringData()
                }
            } catch (e: SQLException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "Failed to Connect", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLTimeoutException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "Connection timeout", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: ClassNotFoundException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "ClassNotFoundException", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            }

        }.start() //end thread

        /*
        var funCode = "V01_SettingQuerypara01"
        var funValues = "'$sSiteNo_GLB';'$sZoneNo_GLB';'${d} 04:00';'${d} 19:59'"

        val formBody = FormBody.Builder()
            .add("FunCode", funCode)
            .add("FunValues", funValues)
            .build()

        val request = Request.Builder()
            .url(BaseUrl)
            .post(formBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val json = response.body!!.string() // 資料只能抓一次
                    listSiteData = Gson().fromJson(json, Array<SiteData>::class.java).toList()

                    if (selectedPara.length == 0 && selectedInv.length == 0) {
                        getActivity()?.runOnUiThread {
                            showAAChart()
                        }
                    } else {
                        // 取得Site Data後，再去抓Inv Data
                        getInverterStringData()
                    }

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("查詢失敗", "$e")
            }
        })
        */
    }

    private fun getInverterStringData() {
        listInvStringData = mutableListOf<InvStringData>()
        Thread(){
            var ResultMsg = ""
            Class.forName("org.postgresql.Driver")
            try {
                //val conn = DriverManager.getConnection("jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm?sslmode=verify-full","motechpvm","-QR-wAfQ6WRno4RGWTOX2g")

                if (ServerConn == null) {
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                if ( !ServerConn!!.isValid(0)) {
                    println("getInverterStringData:" + ServerConn!!.isValid(0))
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                println("getInverterStringData:" + ServerConn!!.isValid(0))
                Thread.sleep(1000) // cockroach request slow
                //val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                //currentDateAndTime = simpleDateFormat.format(Date())
                val dateStart: String = v.txtdate.text.toString() + " 04:00"
                val dateEnd: String = v.txtdate.text.toString() + " 19:59"

                val strSQL = "SELECT TO_CHAR (A.log_time, 'YYYYMMDDHH24MI') as \"sDataKey\", C.modbus_addr as \"nRS485ID\", C.sn_name as \"sSNID\" " +
                        "$selectedPara " +
                        "FROM d_inverter_monitor_data A " +
                        "LEFT JOIN d_site_monitor_data B ON A.log_time = B.log_time " +
                        "LEFT JOIN s_inverter_setting C ON A.inverter_id = C.inverter_id " +
                        "WHERE A.log_time >= '$dateStart' AND A.log_time <= '$dateEnd' " +
                        "$selectedInv " +
                        "ORDER BY A.log_time ASC, C.modbus_addr ASC"

                val statement = ServerConn!!.prepareStatement(strSQL)

                val rs = statement.executeQuery()
                val metaData = rs.metaData
                //val columnCount = metaData.columnCount

                listInvStringData.clear()

                while (rs.next()) {
                    // 创建 InvStringData 对象
                    val invStringData = InvStringData(
                        sDataKey = rs.getString("sDataKey"),
                        nRS485ID = rs.getInt("nRS485ID"),
                        sSNID = rs.getString("sSNID"),
                        nEa = getFloatOrNull(rs, "nEa", metaData),
                        nOVol = getFloatOrNull(rs, "nOVol", metaData),
                        nOCur = getFloatOrNull(rs, "nOCur", metaData),
                        nPpv = getFloatOrNull(rs, "nPpv", metaData),
                        nVpv_A = getFloatOrNull(rs, "nVpv_A", metaData),
                        nVpv_B = getFloatOrNull(rs, "nVpv_B", metaData),
                        nVpv_C = getFloatOrNull(rs, "nVpv_C", metaData),
                        nVpv_D = getFloatOrNull(rs, "nVpv_D", metaData),
                        nVpv_E = getFloatOrNull(rs, "nVpv_E", metaData),
                        nVpv_F = getFloatOrNull(rs, "nVpv_F", metaData),
                        nVpv_G = getFloatOrNull(rs, "nVpv_G", metaData),
                        nVpv_H = getFloatOrNull(rs, "nVpv_H", metaData),
                        nVpv_I = getFloatOrNull(rs, "nVpv_I", metaData),
                        nVpv_J = getFloatOrNull(rs, "nVpv_J", metaData),
                        nVpv_K = getFloatOrNull(rs, "nVpv_K", metaData),
                        nVpv_L = getFloatOrNull(rs, "nVpv_L", metaData),
                        nIpv_A = getFloatOrNull(rs, "nIpv_A", metaData),
                        nIpv_B = getFloatOrNull(rs, "nIpv_B", metaData),
                        nIpv_C = getFloatOrNull(rs, "nIpv_C", metaData),
                        nIpv_D = getFloatOrNull(rs, "nIpv_D", metaData),
                        nIpv_E = getFloatOrNull(rs, "nIpv_E", metaData),
                        nIpv_F = getFloatOrNull(rs, "nIpv_F", metaData),
                        nIpv_G = getFloatOrNull(rs, "nIpv_G", metaData),
                        nIpv_H = getFloatOrNull(rs, "nIpv_H", metaData),
                        nIpv_I = getFloatOrNull(rs, "nIpv_I", metaData),
                        nIpv_J = getFloatOrNull(rs, "nIpv_J", metaData),
                        nIpv_K = getFloatOrNull(rs, "nIpv_K", metaData),
                        nIpv_L = getFloatOrNull(rs, "nIpv_L", metaData)
                    )

                    listInvStringData.add(invStringData)
                }

                /* this has problem
                val rs = statement.executeQuery()
                //var i = 0
                listInvStringData.clear()
                while (rs.next()) {
                    val sDataKey = rs.getString("sDataKey")
                    val nRS485ID = rs.getInt("nRS485ID")
                    val sSNID = rs.getString("sSNID")
                    val nEa: Float? = try {
                        if (rs.findColumn("nEa") > 0) rs.getFloat("nEa").let { if (rs.wasNull()) null else it } else null
                    } catch (e: SQLException) {
                        null // 欄位不存在，返回 null
                    }
                    val nOVol: Float? = rs.getFloat("nOVol").let { if (rs.wasNull()) null else it }
                    val nOCur = rs.getFloat("nOCur")
                    val nPpv = rs.getFloat("nPpv")
                    val nVpv_A = rs.getFloat("nVpv_A")
                    val nVpv_B = rs.getFloat("nVpv_B")
                    val nVpv_C = rs.getFloat("nVpv_C")
                    val nVpv_D = rs.getFloat("nVpv_D")
                    val nVpv_E = rs.getFloat("nVpv_E")
                    val nVpv_F = rs.getFloat("nVpv_F")
                    val nVpv_G = rs.getFloat("nVpv_G")
                    val nVpv_H = rs.getFloat("nVpv_H")
                    val nVpv_I = rs.getFloat("nVpv_I")
                    val nVpv_J = rs.getFloat("nVpv_J")
                    val nVpv_K = rs.getFloat("nVpv_K")
                    val nVpv_L = rs.getFloat("nVpv_L")
                    val nIpv_A = rs.getFloat("nIpv_A")
                    val nIpv_B = rs.getFloat("nIpv_B")
                    val nIpv_C = rs.getFloat("nIpv_C")
                    val nIpv_D = rs.getFloat("nIpv_D")
                    val nIpv_E = rs.getFloat("nIpv_E")
                    val nIpv_F = rs.getFloat("nIpv_F")
                    val nIpv_G = rs.getFloat("nIpv_G")
                    val nIpv_H = rs.getFloat("nIpv_H")
                    val nIpv_I = rs.getFloat("nIpv_I")
                    val nIpv_J = rs.getFloat("nIpv_J")
                    val nIpv_K = rs.getFloat("nIpv_K")
                    val nIpv_L = rs.getFloat("nIpv_L")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sDataKey,$nEa,$nHi,$nTmp \n"

                    listInvStringData.add(InvStringData(sDataKey, nRS485ID, sSNID, nEa, nOVol, nOCur, nPpv, nVpv_A, nVpv_B, nVpv_C, nVpv_D, nVpv_E, nVpv_F, nVpv_G, nVpv_H, nVpv_I, nVpv_J, nVpv_K, nVpv_L, nIpv_A, nIpv_B, nIpv_C, nIpv_D, nIpv_E, nIpv_F, nIpv_G, nIpv_H, nIpv_I, nIpv_J, nIpv_K, nIpv_L))

                }
                */
                //ServerConn.close()

                // 抓完Inv Data後，確保listSiteData和listInvStringData都抓到資料，再進行合併
                alignSiteDateAndInvStringData()
                getActivity()?.runOnUiThread {
                    showAAChart()
                }
            } catch (e: SQLException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "Failed to Connect", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLTimeoutException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "Connection timeout", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: ClassNotFoundException) {
                getActivity()?.runOnUiThread {
                    Toast.makeText(activity, "ClassNotFoundException", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            }

        }.start() //end thread


        /*
        var funCode = "V01_MySolarToday03"
        var funValues = "$selectedPara;'$sSiteNo_GLB';'$sZoneNo_GLB';'${v.txtdate.text.toString()} 04:00';'${v.txtdate.text.toString()} 19:59';${selectedInv}"

        println(funValues)

        val formBody = FormBody.Builder()
            .add("FunCode", funCode)
            .add("FunValues", funValues)
            .build()

        val request = Request.Builder()
            .url(BaseUrl)
            .post(formBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val json = response.body!!.string() // 資料只能抓一次
                    listInvStringData = Gson().fromJson(json, Array<InvStringData>::class.java).toList()

                    // 抓完Inv Data後，確保listSiteData和listInvStringData都抓到資料，再進行合併
                    alignSiteDateAndInvStringData()
                    getActivity()?.runOnUiThread {
                        showAAChart()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("查詢失敗", "$e")
            }
        })
        */
    }

    // 辅助函数，用于获取 Float 或 null
    private fun getFloatOrNull(rs: java.sql.ResultSet, columnName: String, metaData: java.sql.ResultSetMetaData): Float? {
        // 检查列是否存在
        for (i in 1..metaData.columnCount) {
            if (metaData.getColumnName(i).equals(columnName, ignoreCase = true)) {
                return if (rs.getObject(columnName) != null) {
                    val value = rs.getFloat(columnName)
                    if (!rs.wasNull()) {
                        value // 返回实际值
                    } else {
                        null // 如果值为 null
                    }
                } else {
                    null // 列值本身为 null
                }
            }
        }
        return null // 列不存在
    }

    private fun alignSiteDateAndInvStringData() {

        // 看有幾台inverter
        for ((snid, v) in mapInvStringData) {

            mapInvStringData[snid] = mutableListOf()

            // 以siteData的sDataKey為主，找出InvStringData對應的sDataKey
            for (siteData in listSiteData) {


                val isInvExistDataKey = listInvStringData.filter {
                    it.sSNID == snid && it.sDataKey == siteData.sDataKey
                }

                // 如果用SiteData的sDataKey找不到Inverter對應DataKey的資料，就塞空值
                if (isInvExistDataKey.isEmpty()) {
                    val isInvExistSnid = listInverterChkList.filter {
                        it.sSNID == snid
                    }
                    var emptyInvStringData = InvStringData(siteData.sDataKey, isInvExistSnid[0].nRS485ID, snid, null, null, null, null, null,null,null,null,null,null,null,null,null,null,null,null, null,null,null,null,null,null,null,null,null,null,null,null)
                    mapInvStringData[snid]?.add(emptyInvStringData)
                }
                else
                {
                    // Inverter DataKey應該只會符合一筆，所以直接取0
                    mapInvStringData[snid]?.add(isInvExistDataKey[0])
                }
            }
        }

    }

    private fun showAAChart() {

        val xCategory = listSiteData.map { it.sDataKey.substring(8..9) + ":" + it.sDataKey.substring(10..11) }.toTypedArray()

        // 以SiteData的sDataKey當X軸
        val aaChartModel : AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title(sSite_Name_GLB + " 串列分析")
            .subtitle(v.txtdate.text.toString())
            .animationType(AAChartAnimationType.EaseInOutExpo)
            .animationDuration(0)
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .categories(xCategory)

        // 初始化左右Y軸
        val aaYAxisArray = mutableListOf<AAYAxis>()
        initLeftYAxis(aaYAxisArray)
        initRightYAxis(aaYAxisArray)

        // 塞左右Y軸資料
        val aaSeriesElementArray = mutableListOf<AASeriesElement>()
        addLeftYSeries(aaSeriesElementArray, xCategory)
        addRightYSeries(aaSeriesElementArray, xCategory)

        var aaOptions = aaChartModel.aa_toAAOptions()
        aaOptions.yAxisArray(aaYAxisArray.toTypedArray())
            .series(aaSeriesElementArray.toTypedArray())

        v.aa_chart_view2.aa_drawChartWithChartModel(aaChartModel)
        v.aa_chart_view2.aa_drawChartWithChartOptions(aaOptions)
    }

    private fun addRightYSeries(
        aaSeriesElementArray: MutableList<AASeriesElement>,
        xCategory: Array<String>
    ) {

        // for 案場
        for (i in listSelectedPara.indices) {
            var pData: Array<Any>
            pData = emptyArray<Any>()
            var yAxisIndex = 3
            var sName = ""
            when (listSelectedPara[i]) {
                "nHi"    ->  {
                    pData = listSiteData.map { it.nHi }.toTypedArray()
                    yAxisIndex = 3
                    sName = "案場日照計"
                }
                "nTmp"   -> {
                    pData = listSiteData.map { it.nTmp }.toTypedArray()
                    yAxisIndex = 4
                    sName = "案場溫度計"
                }
            }
            if (pData.isNotEmpty()) {
                val aaSeriesElement = AASeriesElement()
                    .name(sName)
                    .type(AAChartType.Spline)
                    .data(pData)
                    .yAxis(yAxisIndex)
                aaSeriesElementArray.add(aaSeriesElement)
            }

        }

        // for inverter
        for ((snid, listInv) in mapInvStringData) {
            for (i in listSelectedPara.indices) {
                var pData: Array<Any?>
                pData = emptyArray<Any?>()
                when (listSelectedPara[i]) {
                    "nOVol" -> pData = listInv.map { it.nOVol }.toTypedArray()
                    "nOCur" -> pData = listInv.map { it.nOCur }.toTypedArray()
                    "nVpv_A" -> pData = listInv.map { it.nVpv_A }.toTypedArray()
                    "nVpv_B" -> pData = listInv.map { it.nVpv_B }.toTypedArray()
                    "nVpv_C" -> pData = listInv.map { it.nVpv_C }.toTypedArray()
                    "nVpv_D" -> pData = listInv.map { it.nVpv_D }.toTypedArray()
                    "nVpv_E" -> pData = listInv.map { it.nVpv_E }.toTypedArray()
                    "nVpv_F" -> pData = listInv.map { it.nVpv_F }.toTypedArray()
                    "nVpv_G" -> pData = listInv.map { it.nVpv_G }.toTypedArray()
                    "nVpv_H" -> pData = listInv.map { it.nVpv_H }.toTypedArray()
                    "nVpv_I" -> pData = listInv.map { it.nVpv_I }.toTypedArray()
                    "nVpv_J" -> pData = listInv.map { it.nVpv_J }.toTypedArray()
                    "nVpv_K" -> pData = listInv.map { it.nVpv_K }.toTypedArray()
                    "nVpv_L" -> pData = listInv.map { it.nVpv_L }.toTypedArray()
                    "nIpv_A" -> pData = listInv.map { it.nIpv_A }.toTypedArray()
                    "nIpv_B" -> pData = listInv.map { it.nIpv_B }.toTypedArray()
                    "nIpv_C" -> pData = listInv.map { it.nIpv_C }.toTypedArray()
                    "nIpv_D" -> pData = listInv.map { it.nIpv_D }.toTypedArray()
                    "nIpv_E" -> pData = listInv.map { it.nIpv_E }.toTypedArray()
                    "nIpv_F" -> pData = listInv.map { it.nIpv_F }.toTypedArray()
                    "nIpv_G" -> pData = listInv.map { it.nIpv_G }.toTypedArray()
                    "nIpv_H" -> pData = listInv.map { it.nIpv_H }.toTypedArray()
                    "nIpv_I" -> pData = listInv.map { it.nIpv_I }.toTypedArray()
                    "nIpv_J" -> pData = listInv.map { it.nIpv_J }.toTypedArray()
                    "nIpv_K" -> pData = listInv.map { it.nIpv_K }.toTypedArray()
                    "nIpv_L" -> pData = listInv.map { it.nIpv_L }.toTypedArray()
                }

                if (pData.isNotEmpty()) {
                    var yAxisIndex = 1
                    if (listSelectedPara[i].contains("nOVol") || listSelectedPara[i].contains("nVpv")) {
                        yAxisIndex = 1
                    } else if (listSelectedPara[i].contains("nOCur") || listSelectedPara[i].contains("nIpv")) {
                        yAxisIndex = 2
                    }

                    // AAChart處理空值的方法
                    /*
                        val pData : MutableList<MutableList<Any?>> = mutableListOf()
                        pData.add(arrayListOf(1, 5))
                        pData.add(arrayListOf(2, null))
                        pData.add(arrayListOf(3, 7))
                     */
                    val pDataWithNull : MutableList<MutableList<Any?>> = mutableListOf()
                    for (i in xCategory.indices) {
                        pDataWithNull.add(mutableListOf(xCategory[i], pData[i]))
                    }

                    val aaSeriesElement = AASeriesElement()
                        .name(snid + " : " + listInv[0].nRS485ID + " " + listSelectedPara[i])
                        .type(AAChartType.Spline)
                        .data(pDataWithNull.toTypedArray())
                        .yAxis(yAxisIndex)
                    aaSeriesElementArray.add(aaSeriesElement)
                }
            }
        }
    }

    private fun addLeftYSeries(
        aaSeriesElementArray: MutableList<AASeriesElement>,
        xCategory: Array<String>
    ) {
        aaSeriesElementArray.add(
            AASeriesElement()
                .name("案場發電功率")
                .data(listSiteData.map { it.nEa }.toTypedArray())
                .yAxis(0)
        )

        for ((snid, listInv) in mapInvStringData) {
            for (i in listSelectedPara.indices) {
                var pData: Array<Any?>
                pData = emptyArray<Any?>()

                when (listSelectedPara[i]) {
                    "nEa" -> pData = listInv.map { it.nEa }.toTypedArray()
                    "nPpv" -> pData = listInv.map { it.nPpv }.toTypedArray()
                }

                if (pData.isNotEmpty()) {
                    var p = listParameterChkList.filter {
                        it.sName == listSelectedPara[i]
                    }

                    val pDataWithNull : MutableList<MutableList<Any?>> = mutableListOf()
                    for (i in xCategory.indices) {
                        pDataWithNull.add(mutableListOf(xCategory[i], pData[i]))
                    }

                    aaSeriesElementArray.add(
                        AASeriesElement()
                            .name(snid + " : " + listInv[0].nRS485ID + " " + p[0].sName2.toString())
                            .type(AAChartType.Spline)
                            .data(pDataWithNull.toTypedArray())
                            .yAxis(0)
                    )
                }
            }
        }
    }

    private fun initRightYAxis(aaYAxisArray: MutableList<AAYAxis>) {
        // 伏特(V)
        val aaYAxis1 = AAYAxis()
            .visible(false)
            .labels(
                AALabels()
                    .enabled(true)//设置 y 轴是否显示数字
            )
            .opposite(true)
            .title(AATitle().text("伏特(V)"))
            .min(0f)
        if (listSelectedPara.contains("nOVol") ||
            listSelectedPara.contains("nVpv_A") ||
            listSelectedPara.contains("nVpv_B") ||
            listSelectedPara.contains("nVpv_C") ||
            listSelectedPara.contains("nVpv_D") ||
            listSelectedPara.contains("nVpv_E") ||
            listSelectedPara.contains("nVpv_F") ||
            listSelectedPara.contains("nVpv_G") ||
            listSelectedPara.contains("nVpv_H") ||
            listSelectedPara.contains("nVpv_I") ||
            listSelectedPara.contains("nVpv_J") ||
            listSelectedPara.contains("nVpv_K") ||
            listSelectedPara.contains("nVpv_L")
            )
        {
            aaYAxis1.visible = true
        }
        aaYAxisArray.add(aaYAxis1)

        // 安培(A)
        val aaYAxis2 = AAYAxis()
            .visible(false)
            .labels(
                AALabels()
                    .enabled(true)//设置 y 轴是否显示数字
            )
            .opposite(true)
            .title(AATitle().text("安培(A)"))
            .min(0f)
        if (listSelectedPara.contains("nOCur") ||
            listSelectedPara.contains("nIpv_A") ||
            listSelectedPara.contains("nIpv_B") ||
            listSelectedPara.contains("nIpv_C") ||
            listSelectedPara.contains("nIpv_D") ||
            listSelectedPara.contains("nIpv_E") ||
            listSelectedPara.contains("nIpv_F") ||
            listSelectedPara.contains("nIpv_G") ||
            listSelectedPara.contains("nIpv_H") ||
            listSelectedPara.contains("nIpv_I") ||
            listSelectedPara.contains("nIpv_J") ||
            listSelectedPara.contains("nIpv_K") ||
            listSelectedPara.contains("nIpv_L")
            )
        {
            aaYAxis2.visible = true
        }
        aaYAxisArray.add(aaYAxis2)

        // W/㎡
        val aaYAxis3 = AAYAxis()
            .visible(false)
            .labels(
                AALabels()
                    .enabled(true)//设置 y 轴是否显示数字
            )
            .opposite(true)
            .title(AATitle().text("W/㎡"))
            .min(0f)
        if (listSelectedPara.contains("nHi")) {
            aaYAxis3.visible = true
        }
        aaYAxisArray.add(aaYAxis3)

        // ℃
        val aaYAxis4 = AAYAxis()
            .visible(false)
            .labels(
                AALabels()
                    .enabled(true)//设置 y 轴是否显示数字
            )
            .opposite(true)
            .title(AATitle().text("℃"))
            .min(0f)
        if (listSelectedPara.contains("nTmp")) {
            aaYAxis4.visible = true
        }
        aaYAxisArray.add(aaYAxis4)
    }

    private fun initLeftYAxis(aaYAxisArray: MutableList<AAYAxis>) {
        // 左邊Y軸設定 (Site nEa, Inv nEa, Inv nPpv 在左邊)
        val aaYAxis0 = AAYAxis()
            .visible(true)
            .labels(
                AALabels()
                    .enabled(true)//设置 y 轴是否显示数字
                    .style(
                        AAStyle()
                            .color("#ff0000")//yAxis Label font color
                            .fontSize(12f)//yAxis Label font size
                            .fontWeight(AAChartFontWeightType.Bold)//yAxis Label font weight
                    )
            )
            .gridLineWidth(0f)// Y 轴网格线宽度
            .title(AATitle().text("即時發電功率(kW)"))//Y 轴标题
            .min(0f)


        aaYAxisArray.add(aaYAxis0)
    }

    private fun setDateFormat(year: Int, month: Int, day: Int): String {
        var strMonth : String
        var strDay : String
        if ((month + 1) < 10) {
            strMonth = "0" + (month+1).toString()
        } else {
            strMonth = (month+1).toString()
        }

        if(day < 10){
            strDay  = "0" + day.toString()
        } else {
            strDay = day.toString()
        }

        return "$year-${strMonth}-$strDay"
    }
}