package tw.com.motech.i_inverter_idv

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_a.*
import kotlinx.android.synthetic.main.fragment_a.view.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import tw.com.motech.i_inverter_idv.R
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class FragmentA : Fragment() {
    var sitedata = mutableListOf<SiteData>()
    var lastsitedata = mutableListOf<LastSiteData>()
    var currentDateAndTime: String = ""
    var currentDateNoDash: String = ""
    private lateinit var v:View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         v = inflater.inflate(R.layout.fragment_a, container, false)
        //val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        /*
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val currentDateAndTime: String = simpleDateFormat.format(Date())
        val dateStr: String = currentDateAndTime + " 05:00"
        val dateEnd: String = currentDateAndTime + " 18:59"

        v.txtfrag1.setText("Fragment A SiteNo:{$sSiteNo_GLB}, ZoneNo:{$sZoneNo_GLB}")
        v.txtfrag2.setText("Hello A {$sSite_Name_GLB}, dateStr= ${dateStr}, dateEnd= ${dateEnd}")

         */

        /*
        val txt1:TextView = v.findViewById(R.id.txtfrag1) as TextView
        val txt2:TextView = v.findViewById(R.id.txtfrag2) as TextView
        txt1.setText("Fragment A {$sSiteNo_GLB}")
        txt2.setText("Hello A {$sSite_Name_GLB}")
         */
        Thread.sleep(1000) // cockroach request slow
        getSiteData()
        Thread.sleep(1000) //兩個Thread會互相干擾，所以要sleep
        getLastSiteData()
        return v
    }

    private fun getSiteData() {
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

                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                currentDateAndTime = simpleDateFormat.format(Date())
                val dateStart: String = currentDateAndTime + " 05:00"
                val dateEnd: String = currentDateAndTime + " 18:59"

                val strSQL = "SELECT TO_CHAR (log_time, 'YYYYMMDDHH24MI') as \"sDataKey\", " +
                        "ROUND((pac_all/1000),2) as \"nEa\", sunshine as \"nHi\", moduletemp as \"nTmp\" " +
                        "FROM d_site_monitor_data " +
                        "WHERE log_time >= '$dateStart' AND log_time <= '$dateEnd' " +
                        "ORDER BY log_time ASC"

                val statement = ServerConn!!.prepareStatement(strSQL)
                val rs = statement.executeQuery()
                var i = 0
                sitedata.clear()
                while (rs.next()) {
                    val sDataKey = rs.getString("sDataKey")
                    val nEa = rs.getDouble("nEa")
                    val nHi = rs.getFloat("nHi")
                    val nTmp = rs.getFloat("nTmp")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sDataKey,$nEa,$nHi,$nTmp \n"
                    i++
                    if ((i % 3) == 0) {  //因資料太細，每三筆顯示一次
                        sitedata.add(SiteData(sDataKey,nEa,nHi,nTmp))
                    }
                }
                //ServerConn.close()
                getActivity()?.runOnUiThread {
                    //Toast.makeText(activity, ResultMsg, Toast.LENGTH_LONG).show()
                    if(sitedata.isNotEmpty()) {
                        showAAChart()
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

        }.start() //end thread
        /*
        Thread(){
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            currentDateAndTime = simpleDateFormat.format(Date())
            val dateStart: String = currentDateAndTime + " 05:00"
            val dateEnd: String = currentDateAndTime + " 18:59"

            val requestBody = FormBody.Builder()
                .add("FunCode", "V01_SettingQuerypara01")
                .add("FunValues", "'${sSiteNo_GLB}';'${sZoneNo_GLB}';'${dateStart}';'${dateEnd}'")
                .build()

            val request = Request.Builder().url(BaseUrl)
                .post(requestBody).build()

            val response = client.newCall(request).execute()
            val responsestr = response.body?.string()

            sitedata = Gson().fromJson(responsestr, Array<SiteData>::class.java).toList()
            getActivity()?.runOnUiThread {
                if(sitedata.count() > 0) {
                    showAAChart()
                }
            }
        }.start()
        */
    }

    private fun getLastSiteData() {
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
                    println("getLastSiteData:" + ServerConn!!.isValid(0))
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }
                println("getLastSiteData:" + ServerConn!!.isValid(0))

                val strSQL = "SELECT TO_CHAR (log_time, 'YYYYMMDDHH24MI') as \"sDataKey\", " +
                        "(SELECT Site_ID FROM s_site_setting) as \"sSiteNo\", " +
                        "ROUND((pac_all/1000),2) as \"nEa\", " +
                        "sunshine as \"nHi\", " +
                        "moduletemp as \"nTmp\", " +
                        "etoday as \"nTEa\", " +
                        "( " +
                        "  SELECT ROUND(MAX(pac_all)/1000,2) " +
                        "  FROM d_site_monitor_data " +
                        "  WHERE DATE(Log_Time) = (current_timestamp AT TIME ZONE 'Asia/Taipei')::date " +
                        ") as \"nEaMax\" " +
                        "FROM d_site_monitor_current"

                val statement = ServerConn!!.prepareStatement(strSQL)
                val rs = statement.executeQuery()
                lastsitedata.clear()
                while (rs.next()) {
                    val sDataKey = rs.getString("sDataKey")
                    val sSiteNo = rs.getString("sSiteNo")
                    val nEa = rs.getDouble("nEa")
                    val nHi = rs.getFloat("nHi")
                    val nTmp = rs.getFloat("nTmp")
                    val nTEa = rs.getDouble("nTEa")
                    val nEaMax = rs.getDouble("nEaMax")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sDataKey,$nEa,$nHi,$nTmp \n"
                    lastsitedata.add(LastSiteData(sDataKey,sSiteNo,nEa,nHi,nTmp,nTEa,nEaMax))
                }
                //ServerConn.close()
                getActivity()?.runOnUiThread {
                    if(lastsitedata.count() > 0) {
                        v.txtAcum.setText("今日累積:${lastsitedata[0].nTEa}kWh")
                        v.txtMaxEa.setText("今日最高:${lastsitedata[0].nEaMax}kw")
                        v.txtRealEa.setText("及時發電功率:${lastsitedata[0].nEa}kw")
                        v.txtHi.setText("實體日照:${lastsitedata[0].nHi}W/m2")
                        v.txtTmp.setText("模組溫度:${lastsitedata[0].nTmp}℃")
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

        }.start() //end thread

        /*
        Thread(){
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd")
            currentDateNoDash = simpleDateFormat.format(Date())


            val requestBody = FormBody.Builder()
                .add("FunCode", "V04_AppToday01")
                .add("FunValues", "${sSiteNo_GLB};${sZoneNo_GLB};${currentDateNoDash}")
                .build()

            val request = Request.Builder().url(BaseUrl)
                .post(requestBody).build()

            val response = client.newCall(request).execute()
            val responsestr = response.body?.string()

            lastsitedata = Gson().fromJson(responsestr, Array<LastSiteData>::class.java).toList()
            getActivity()?.runOnUiThread {
                if(lastsitedata.count() > 0) {
                    v.txtAcum.setText("今日累積:${lastsitedata[0].nTEa}kWh")
                    v.txtMaxEa.setText("今日最高:${lastsitedata[0].nEaMax}kw")
                    v.txtRealEa.setText("及時發電功率:${lastsitedata[0].nEa}kw")
                    v.txtHi.setText("實體日照:${lastsitedata[0].nHi}W/m2")
                    v.txtTmp.setText("模組溫度:${lastsitedata[0].nTmp}℃")
                }
            }
        }.start()
        */
    }

    private fun showAAChart() {
        val aaChartModel : AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title(sSite_Name_GLB + " 發電功率")
            .subtitle(currentDateAndTime + " 日")
            .animationType(AAChartAnimationType.EaseInOutExpo)
            .animationDuration(0)
            .backgroundColor("#FFFFFF")
            .dataLabelsEnabled(true)
            .categories(sitedata.map { it.sDataKey.substring(8..9) + ":" + it.sDataKey.substring(10..11) }.toTypedArray())


        var aaOptions = aaChartModel.aa_toAAOptions()
        val aaYAxis0 = AAYAxis()
            .visible(true)
            .labels(AALabels()
                .enabled(true)//设置 y 轴是否显示数字
                .style(AAStyle()
                    .color("#ff0000")//yAxis Label font color
                    .fontSize(12f)//yAxis Label font size
                    .fontWeight(AAChartFontWeightType.Bold)//yAxis Label font weight
                ))
            .gridLineWidth(0f)// Y 轴网格线宽度
            .title(AATitle().text("發電量"))//Y 轴标题
            .min(0f)

        val aaYAxis1 = AAYAxis()
            .visible(true)
            .labels(AALabels()
                .enabled(true)//设置 y 轴是否显示数字
            )
            .opposite(true)
            .title(AATitle().text("日照量"))
            .min(0f)

        val aaYAxis2 = AAYAxis()
            .visible(true)
            .labels(AALabels()
                .enabled(true)//设置 y 轴是否显示数字
            )
            .opposite(true)
            .title(AATitle().text("溫度"))
            .min(0f)
/*
        val aaTooltip = AATooltip()
            .useHTML(true)
            .formatter(
                """
function () {
        return ' 🌕 🌖 🌗 🌘 🌑 🌒 🌓 🌔 <br/> '
        + ' Support JavaScript Function Just Right Now !!! <br/> '
        + ' The Gold Price For <b>2020 '
        +  this.x
        + ' </b> Is <b> '
        +  this.y
        + ' </b> Dollars ';
        }
             """.trimIndent()
            )
            .valueDecimals(2)//設置取值精確到小數點後幾位//設置取值精確到小數點後幾位
            .backgroundColor("#000000")
            .borderColor("#000000")
            .style(
                AAStyle()
                    .color("#FFD700")
                    .fontSize(12f)
            )

 */

        aaOptions.yAxisArray(arrayOf(aaYAxis0, aaYAxis1, aaYAxis2))
            .series(arrayOf(
                AASeriesElement()
                    .name("發電量1")
                    .data(sitedata.map { it.nEa }.toTypedArray())
                    .yAxis(0),
                AASeriesElement()
                    .name("日照量1")
                    .type(AAChartType.Spline)
                    .data(sitedata.map { it.nHi }.toTypedArray())
                    .yAxis(1),
                AASeriesElement()
                    .name("溫度1")
                    .type(AAChartType.Spline)
                    .data(sitedata.map { it.nTmp }.toTypedArray())
                    .yAxis(2)
            )
            )
                /*
            .tooltip(aaTooltip)
                 */

        v.aa_chart_view.aa_drawChartWithChartModel(aaChartModel)
        v.aa_chart_view.aa_drawChartWithChartOptions(aaOptions)

    }
}