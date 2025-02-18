package tw.com.motech.i_inverter_idv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_site_list.*
import okhttp3.*
import java.io.IOException
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class SiteListActivity : AppCompatActivity() {
    private lateinit var adapter: SiteListAdapterActivity
    private lateinit var siteresults: List<SiteResult>
    //private lateinit var siteresults_all: List<SiteResult>
    private var siteresults_all = mutableListOf<SiteResult>()
    private var type = "1"
    private var typeName = "北"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site_list)
        getSiteInfo()


        siteresults_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        // 北中南切頁事件
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        type = "1"
                        typeName = "北"
                    }
                    1 -> {
                        type = "2"
                        typeName = "中"
                    }
                    2 -> {
                        type = "3"
                        typeName = "南"
                    }
                }
                getPartSiteInfo()
            }

        })
    }

    private fun setBarTitle() {
        this@SiteListActivity.supportActionBar!!.title = "${typeName}部案場 (${adapter.itemCount})"
    }

    private fun getSiteInfo(){

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
                    println("getSiteInfo:" + ServerConn!!.isValid(0))
                    ServerConn = DriverManager.getConnection(
                        ServerUrl,
                        UserName,
                        PassWord
                    )
                }

                println("getSiteInfo:" + ServerConn!!.isValid(0))
                //Thread.sleep(1000) // cockroach request slow

                val strSQL = "select " +
                        "(select site_location from s_site_setting) as \"sSiteType\", " +
                        "(select site_location from s_site_setting) as \"nSort\", " +
                        "(select Site_ID from s_site_setting) as \"sSiteNo\", " +
                        "(select Site_Name from s_site_setting) as \"sSite_Name\", " +
                        "IF((select count(*) from d_inverter_monitor_current where Process_State <> 'Normal') > 0,-5,1) as \"nSHI\", " +
                        "ROUND((Etoday/(select capacity from s_site_setting)),2) as \"nDMY\", " +
                        "'83.3' as \"nPR\" " +
                        "from d_site_monitor_current " +
                        "where DATE(Log_Time) = (current_timestamp AT TIME ZONE 'Asia/Taipei')::date "

                //val strSQL = "select * from d_inverter_monitor_current" //for test
                /*
                val strSQL = "select DATE(Log_Time) as log_time, " +
                        "(current_timestamp AT TIME ZONE 'Asia/Taipei')::date " +
                        "from d_site_monitor_current"
                */ //for test

                //val strSQL = "select current_timestamp AT TIME ZONE 'Asia/Taipei'" //for test
                //val strSQL = "show TIME ZONE" //for test

                val statement = ServerConn!!.prepareStatement(strSQL)
                val rs = statement.executeQuery()
                siteresults_all.clear()
                while (rs.next()) {
                    //val sSiteType = rs.getString("log_time") //for test
                    //val sSiteNo = rs.getString("timezone") //for test
                    val sSiteType = rs.getString("sSiteType")
                    val nSort = rs.getInt("nSort")
                    val sSiteNo = rs.getString("sSiteNo")
                    val sSite_Name = rs.getString("sSite_Name")
                    val nSHI = rs.getInt("nSHI")
                    val nDMY = rs.getDouble("nDMY")
                    val nPR = rs.getDouble("nPR")
                    //println("LogTime:$log_time")
                    //ResultMsg += "$sSiteType,$nSort,$sSiteNo,$sSite_Name,$nSHI,$nDMY,$nPR \n"
                    siteresults_all.add(SiteResult(sSiteType,nSort,sSiteNo,sSite_Name,nSHI,nDMY,nPR))
                }
                //ServerConn.close()
                siteresults = siteresults_all.filter { it.sSiteType == type }

                runOnUiThread {
                    //Toast.makeText(this, ResultMsg, Toast.LENGTH_LONG).show()
                    showRecycleView()
                    setBarTitle()
                }
            } catch (e: SQLException) {
                runOnUiThread {
                    Toast.makeText(this, "Failed to Connect", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: SQLTimeoutException) {
                runOnUiThread {
                    Toast.makeText(this, "Connection timeout", Toast.LENGTH_LONG).show()
                }

                Log.e(this::class.toString(), e.message, e)
            } catch (e: ClassNotFoundException) {
                runOnUiThread {
                    Toast.makeText(this, "ClassNotFoundException", Toast.LENGTH_LONG).show()
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
                .add("FunCode", "V02_RwdDashboard04")
                .add("FunValues", "'${UserName}'")
                .build()

            val request = Request.Builder().url(BaseUrl)
                .post(requestBody).build()

            val response = client.newCall(request).execute()
            val responsestr = response.body?.string()

            siteresults_all = Gson().fromJson(responsestr, Array<SiteResult>::class.java).toList()
            siteresults = siteresults_all.filter { it.sSiteType == type }

            runOnUiThread {
                showRecycleView()
                setBarTitle()
            }
        }.start()
         */
    }

    private fun getPartSiteInfo(){
        siteresults = siteresults_all.filter { it.sSiteType == type }
        showRecycleView()
        setBarTitle()
    }

    private fun  showRecycleView(){
        /*
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        adapter = MyAdapter(siteresults)
        recyclerView.adapter = adapter
        */
        val gridLayoutManager = GridLayoutManager(this, 2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        recyclerView.layoutManager = gridLayoutManager
        adapter = SiteListAdapterActivity(siteresults)
        recyclerView.adapter = adapter
    }
}