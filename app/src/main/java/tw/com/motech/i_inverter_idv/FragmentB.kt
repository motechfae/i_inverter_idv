package tw.com.motech.i_inverter_idv

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_a.view.*
import kotlinx.android.synthetic.main.fragment_b.*
import kotlinx.android.synthetic.main.fragment_b.view.*
import okhttp3.*
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class FragmentB : Fragment() {
    private lateinit var adapter_inv: InverterListAdapterActivity
    //private lateinit var inverterresults: List<InverterResult>
    private lateinit var inverterresults: MutableList<InverterResult>
    //var inverterresults = mutableListOf<InverterResult>()
    private lateinit var v:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_b, container, false)
        inverterresults = mutableListOf<InverterResult>()
        getInverterData()
        return v
    }

    private fun getInverterData() {
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
                    println("getInverterData:" + ServerConn!!.isValid(0))
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                println("getInverterData:" + ServerConn!!.isValid(0))
                Thread.sleep(1000) // cockroach request slow
                val strSQL = "SELECT LPAD(CAST(A.inverter_id as TEXT),2,'0') as \"sNo\", '000' as \"sDataKey\", A.sn_name as \"sSNID\", " +
                        "A.modbus_addr as \"nRS485ID\", ROUND(B.pac_all,2) as \"nEa\", " +
                        "COALESCE ( " +
                        "  ( " +
                        "    SELECT C.err_desc FROM d_inverter_error_current C " +
                        "    WHERE C.inverter_id = A.inverter_id " +
                        "    and C.log_time > (NOW() AT TIME ZONE 'Asia/Taipei' - INTERVAL '60 minutes') " +
                        "   ), '' " +
                        ")  as \"sErrCode\", " +
                        "IF(B.pac_all > 0,1,0) as \"ConChk\", TO_CHAR (B.log_time, 'YYYY-MM-DD HH24:MI:SS') as \"dCreat_Time\", " +
                        "(A.brand_name || '_' || A.model_type) as \"sInvModel\" " +
                        "FROM s_inverter_setting A " +
                        "LEFT JOIN d_inverter_monitor_current B ON A.inverter_id = B.inverter_id " +
                        "WHERE online = 'Y' " +
                        "ORDER BY A.inverter_id"

                val statement = ServerConn!!.prepareStatement(strSQL)
                val rs = statement.executeQuery()
                inverterresults.clear()
                while (rs.next()) {
                    val sNo= rs.getString("sNo")
                    val sDataKey = rs.getString("sDataKey")
                    val sSNID = rs.getString("sSNID")
                    val nRS485ID = rs.getInt("nRS485ID")
                    val nEa = rs.getDouble("nEa")
                    val sErrCode = rs.getString("sErrCode")
                    val ConChk = rs.getInt("ConChk")
                    val dCreat_Time = rs.getString("dCreat_Time")
                    val sInvModel = rs.getString("sInvModel")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sDataKey,$nEa,$nHi,$nTmp \n"
                    inverterresults.add(InverterResult(sNo,sDataKey,sSNID,nRS485ID,nEa,sErrCode,ConChk,dCreat_Time,sInvModel))
                }
                //inverterresults = inverterresults_tmp.toList()
                //ServerConn.close()
                getActivity()?.runOnUiThread {
                    if(inverterresults.count() > 0) {
                        showRecycleView()
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
        Thread(){
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val requestBody = FormBody.Builder()
                .add("FunCode", "V01_MySolarToday08")
                .add("FunValues", "'${sSiteNo_GLB}';'${sZoneNo_GLB}'")
                .build()

            val request = Request.Builder().url(BaseUrl)
                .post(requestBody).build()

            val response = client.newCall(request).execute()
            val responsestr = response.body?.string()

            inverterresults = Gson().fromJson(responsestr, Array<InverterResult>::class.java).toList()
            getActivity()?.runOnUiThread {
                if(inverterresults.count() > 0) {
                    showRecycleView()
                }
            }
        }.start()
        */
    }

    private fun  showRecycleView(){

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        v.invrecview.layoutManager = linearLayoutManager
        adapter_inv = InverterListAdapterActivity(inverterresults)
        v.invrecview.adapter = adapter_inv

    }
}