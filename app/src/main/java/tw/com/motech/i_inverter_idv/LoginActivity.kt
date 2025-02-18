package tw.com.motech.i_inverter_idv

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import kotlin.concurrent.thread


private val client = OkHttpClient()

class LoginResult(
    val sAccount: String,
    val sPassword: String,
    val sAccountName: String,
    val sEMail: String,
    val sTEL: String,
    val sPhone: String,
    val sAuthority: String
)

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*
        if(android.os.Build.VERSION.SDK_INT > 9){
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
       */

        // 使用先前存的username password登入
        var sharedPreference = getSharedPreferences(LoginSP, Context.MODE_PRIVATE)
        var serverpos = sharedPreference.getInt("server",-1)
        val username = sharedPreference.getString("username", null)
        val password = sharedPreference.getString("password", null)

        var sp_server = findViewById<Spinner>(R.id.sp_server)
        var et_username = findViewById<EditText>(R.id.et_username)
        var et_password = findViewById<EditText>(R.id.et_password)
        if (username != null && password != null && serverpos != -1) {
            et_username.setText(username)
            et_password.setText(password)
            sp_server.setSelection(serverpos)
            postLogin(username, password, serverpos)
        }
        //sp_server.setSelection(1)

        // 使用按鈕登入
        var btn_login = findViewById<Button>(R.id.btn_login)
        btn_login.setOnClickListener {
            postLogin(et_username.text.toString(), et_password.text.toString(), sp_server.selectedItemPosition)
            /*
            thread {
                var ResultMsg = ""
                Class.forName("org.postgresql.Driver")
                try {
                    //val conn = DriverManager.getConnection("jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm?sslmode=verify-full","motechpvm","-QR-wAfQ6WRno4RGWTOX2g")
                    val conn = DriverManager.getConnection(
                        "jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm",
                        "joewangtw",
                        "1qaz@WSX"
                    )

                    println(conn.isValid(0))
                    val statement =
                        conn.prepareStatement("select * from d_inverter_monitor_current")
                    val rs = statement.executeQuery()
                    while (rs.next()) {
                        val log_time = rs.getString("Log_Time")
                        //println("LogTime:$log_time")
                        ResultMsg += "log_time: $log_time\n"
                    }
                    conn.close()

                    runOnUiThread {
                        Toast.makeText(this, ResultMsg, Toast.LENGTH_LONG).show()
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
            */
        }
    }

    private fun postLogin(username: String?, password: String?, serverpos: Int?){
        var serverurl = ""
        when (serverpos){
            0 -> {
                serverurl = "jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm"
                //Toast.makeText(this, ServerUrl, Toast.LENGTH_LONG).show()
            }
            1 -> {
                serverurl = "in test"
                //Toast.makeText(this, ServerUrl, Toast.LENGTH_LONG).show()
            }
        } // end of when

        thread {
            var ResultMsg = ""
            Class.forName("org.postgresql.Driver")
            try {
                //val conn = DriverManager.getConnection("jdbc:postgresql://motechpvmcluster-8718.8nk.gcp-asia-southeast1.cockroachlabs.cloud:26257/motech_pvm?sslmode=verify-full","motechpvm","-QR-wAfQ6WRno4RGWTOX2g")

                if (ServerConn == null) {
                    ServerConn = DriverManager.getConnection(
                        serverurl,
                        username,
                        password
                    )
                }
                if ( ServerConn!!.isValid(0)) {
                    ServerUrl = serverurl
                    UserName = username.toString()
                    PassWord = password.toString()
                    startActivity(Intent(this@LoginActivity, SiteListActivity::class.java))
                    /*
                    runOnUiThread{
                        Toast.makeText(this@LoginActivity, "登入成功，跳轉至功能頁面!", Toast.LENGTH_SHORT).show()
                    }
                    */
                    // 存到SharedPreferences裡面，下次開啟APP直接登入
                    var chk_RememberMe = findViewById<CheckBox>(R.id.chk_RememberMe)
                    if (chk_RememberMe.isChecked) {
                        val editor =
                            getSharedPreferences(LoginSP, Context.MODE_PRIVATE).edit()
                        editor.putInt("server",serverpos!!.toInt())
                        editor.putString("username", username.toString())
                        editor.putString("password", password.toString())
                        editor.commit()
                    }
                }
                /*
                println(ServerConn!!.isValid(0))
                val statement =
                    ServerConn!!.prepareStatement("select * from d_inverter_monitor_current")
                val rs = statement.executeQuery()
                while (rs.next()) {
                    val log_time = rs.getString("Log_Time")
                    //println("LogTime:$log_time")
                    ResultMsg += "log_time: $log_time\n"
                }
                //ServerConn.close()

                runOnUiThread {
                    Toast.makeText(this, ResultMsg, Toast.LENGTH_LONG).show()
                }
                */
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
    }

    private fun postLogin(username: String?, password: String?) {
        val formBody = FormBody.Builder()
            .add("FunCode", "V01_Login01")
            .add("FunValues", "'${username}';'${password}'")
            .build()
        val request = Request.Builder()
            .url(BaseUrl)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val json = response.body!!.string() // 資料只能抓一次
                    val list: List<LoginResult> =
                        Gson().fromJson(json, Array<LoginResult>::class.java).toList()

                    if (list.count() == 0) {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "帳密錯誤，登入失敗!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        for (l in list) {
                            println("sAccount: ${l.sAccount}, sPassword: ${l.sPassword}")
                        }
                        if (username.equals(list[0].sAccount) && password.equals(list[0].sPassword)) {

                            UserName = list[0].sAccount
                            startActivity(Intent(this@LoginActivity, SiteListActivity::class.java))
                            /*
                            runOnUiThread{
                                Toast.makeText(this@LoginActivity, "登入成功，跳轉至功能頁面!", Toast.LENGTH_SHORT).show()
                            }
                            */
                            // 存到SharedPreferences裡面，下次開啟APP直接登入
                            var chk_RememberMe = findViewById<CheckBox>(R.id.chk_RememberMe)
                            if (chk_RememberMe.isChecked) {
                                val editor =
                                    getSharedPreferences(LoginSP, Context.MODE_PRIVATE).edit()
                                editor.putString("username", list[0].sAccount)
                                editor.putString("password", list[0].sPassword)
                                editor.commit()
                            }
                        } else{
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "帳密不正確，登入失敗!", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("查詢失敗", "$e")
            }
        })
    }
}