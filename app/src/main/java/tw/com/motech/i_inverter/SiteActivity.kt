package tw.com.motech.i_inverter_idv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import kotlin.math.abs

private val client = OkHttpClient()
/*
class SiteResult(
    val sSiteType: String,
    val nSort: Int,
    val sSiteNo: String,
    val sSite_Name: String,
    val nSHI: Int,
    val nDMY: Double,
    val nPR: Double
)
*/
class SiteActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var r11: LinearLayout
    private lateinit var r11_btn: ImageButton
    private lateinit var r11_tv: TextView
    private lateinit var r12: LinearLayout
    private lateinit var r12_btn: ImageButton
    private lateinit var r12_tv: TextView
    private lateinit var r21: LinearLayout
    private lateinit var r21_btn: ImageButton
    private lateinit var r21_tv: TextView
    private lateinit var r22: LinearLayout
    private lateinit var r22_btn: ImageButton
    private lateinit var r22_tv: TextView
    private lateinit var r31: LinearLayout
    private lateinit var r31_btn: ImageButton
    private lateinit var r31_tv: TextView
    private lateinit var r32: LinearLayout
    private lateinit var r32_btn: ImageButton
    private lateinit var r32_tv: TextView
    private lateinit var r41: LinearLayout
    private lateinit var r41_btn: ImageButton
    private lateinit var r41_tv: TextView
    private lateinit var r42: LinearLayout
    private lateinit var r42_btn: ImageButton
    private lateinit var r42_tv: TextView
    private lateinit var r51: LinearLayout
    private lateinit var r51_btn: ImageButton
    private lateinit var r51_tv: TextView
    private lateinit var r52: LinearLayout
    private lateinit var r52_btn: ImageButton
    private lateinit var r52_tv: TextView
    private lateinit var images: ArrayList<ImageButton>
    private lateinit var tvs: ArrayList<TextView>
    private lateinit var lls: ArrayList<LinearLayout>

    private lateinit var btnPre: Button
    private lateinit var btnNext: Button

    private lateinit var tabs : TabLayout

    private val SITE_PER_PAGE = 10
    private var currPage = 1
    private var pages = 1
    private lateinit var list: List<SiteResult>

    private lateinit var mDetector: GestureDetectorCompat
    private var start_move_x = 0.0f
    private var end_move_x = 0.0f
    private var is_move = false

    private var type = "1"
    private var typeName = "北"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site)
        mDetector = GestureDetectorCompat(this, this)

        getSiteType()
        fundById()
        getSiteInfo()

        btnPre.setOnClickListener {
            prePage()
        }

        btnNext.setOnClickListener {
            nextPage()
        }

        /* 跟ScrollView滑動事件衝到，滑動行為怪怪的
        // 複寫每一個ImageButton左移右移的事件
        images.forEachIndexed { index, imageButton ->
            imageButton.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    //println("onTouch : ${event}")
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            start_move_x = event.x
                            is_move = false
                        }
                       MotionEvent.ACTION_MOVE -> {
                            end_move_x = event.x
                            is_move = true
                        }
                        MotionEvent.ACTION_UP -> {
                            if (is_move) {
                                //println("換頁")
                                if (start_move_x < end_move_x) {
                                    prePage()
                                } else {
                                    nextPage()
                                }
                            } else {
                                Toast.makeText(v?.context, "切到${tvs[index].text}案場", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@SiteActivity, MainActivity::class.java))
                            }

                        }
                    }
                    return v?.onTouchEvent(event) ?: true
                }
            })
        }
        */

        images.forEachIndexed { index, imageButton ->
            imageButton.setOnClickListener {
                startActivity(Intent(this@SiteActivity, SiteFuncActivity::class.java))
            }
        }

        tvs.forEach { textView ->
            textView.text = "下載中..."
        }

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
                getSiteInfo()
            }

        })
    }

    private fun getSiteType() {
        // 取得北中南東的位置
        // 原先Login切到Taiwan, 現在改Login切到Site
        /*
        intent?.extras?.let {
            type = it.getString("sSiteType").toString()
            when(type) {
                "1" -> typeName = "北"
                "2" -> typeName = "中"
                "3" -> typeName = "南"
            }
        }
        */

        typeName = "北"
    }

    private fun nextPage() {
        if (currPage < pages) {
            currPage++
            setBarTitle()

            var begin = (currPage - 1) * SITE_PER_PAGE
            var end = begin + SITE_PER_PAGE - 1
            // 如果是最後一頁，取餘數，避免超出陣列範圍
            if (currPage == pages) {
                end = begin + list.count() % SITE_PER_PAGE - 1
            }

            update(begin, end)
        }
    }

    private fun prePage() {
        if (currPage > 1) {
            currPage--;
            setBarTitle()

            var begin = (currPage - 1) * SITE_PER_PAGE
            var end = begin + SITE_PER_PAGE - 1

            // 如果是第一頁，取餘數，避免超出陣列範圍
            if (currPage == 1 && list.count() < SITE_PER_PAGE) {
                end = list.count() % SITE_PER_PAGE
            }

            update(begin, end)
        }
    }

    private fun setBarTitle() {
        this@SiteActivity.supportActionBar!!.title = "${typeName}部案場 (${currPage}/${pages})"
    }

    private fun fundById() {

        r11_btn = findViewById<ImageButton>(R.id.r11_btn)
        r12_btn = findViewById<ImageButton>(R.id.r12_btn)
        r21_btn = findViewById<ImageButton>(R.id.r21_btn)
        r22_btn = findViewById<ImageButton>(R.id.r22_btn)
        r31_btn = findViewById<ImageButton>(R.id.r31_btn)
        r32_btn = findViewById<ImageButton>(R.id.r32_btn)
        r41_btn = findViewById<ImageButton>(R.id.r41_btn)
        r42_btn = findViewById<ImageButton>(R.id.r42_btn)
        r51_btn = findViewById<ImageButton>(R.id.r51_btn)
        r52_btn = findViewById<ImageButton>(R.id.r52_btn)
        images = arrayListOf<ImageButton>(
            r11_btn,
            r12_btn,
            r21_btn,
            r22_btn,
            r31_btn,
            r32_btn,
            r41_btn,
            r42_btn,
            r51_btn,
            r52_btn
        )

        r11_tv = findViewById<TextView>(R.id.r11_tv)
        r12_tv = findViewById<TextView>(R.id.r12_tv)
        r21_tv = findViewById<TextView>(R.id.r21_tv)
        r22_tv = findViewById<TextView>(R.id.r22_tv)
        r31_tv = findViewById<TextView>(R.id.r31_tv)
        r32_tv = findViewById<TextView>(R.id.r32_tv)
        r41_tv = findViewById<TextView>(R.id.r41_tv)
        r42_tv = findViewById<TextView>(R.id.r42_tv)
        r51_tv = findViewById<TextView>(R.id.r51_tv)
        r52_tv = findViewById<TextView>(R.id.r52_tv)
        tvs = arrayListOf<TextView>(
            r11_tv,
            r12_tv,
            r21_tv,
            r22_tv,
            r31_tv,
            r32_tv,
            r41_tv,
            r42_tv,
            r51_tv,
            r52_tv
        )

        r11 = findViewById<LinearLayout>(R.id.r11)
        r12 = findViewById<LinearLayout>(R.id.r12)
        r21 = findViewById<LinearLayout>(R.id.r21)
        r22 = findViewById<LinearLayout>(R.id.r22)
        r31 = findViewById<LinearLayout>(R.id.r31)
        r32 = findViewById<LinearLayout>(R.id.r32)
        r41 = findViewById<LinearLayout>(R.id.r41)
        r42 = findViewById<LinearLayout>(R.id.r42)
        r51 = findViewById<LinearLayout>(R.id.r51)
        r52 = findViewById<LinearLayout>(R.id.r52)
        lls = arrayListOf<LinearLayout>(r11, r12, r21, r22, r31, r32, r41, r42, r51, r52)

        btnPre = findViewById<Button>(R.id.btnPre)
        btnNext = findViewById<Button>(R.id.btnNext)

        tabs = findViewById<TabLayout>(R.id.tabs)
    }

    private fun getSiteInfo() {
        var funCode = "V02_RwdDashboard04"
        var funValues = UserName
        println("FunCode:${funCode}, FunValues:${funValues}")

        val formBody = FormBody.Builder()
            .add("FunCode", funCode)
            .add("FunValues", "'${funValues}'")
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
                    list = Gson().fromJson(json, Array<SiteResult>::class.java).toList()
                    list = list.filter { it.sSiteType == type }
                    /*
                    for (l in list) {
                        println("sSite_Name: ${l.sSite_Name}, nSHI: ${l.nSHI}")
                    }
                    */
                    runOnUiThread {
                        // 頁數
                        pages = (list.count() / SITE_PER_PAGE) + 1
                        setBarTitle()

                        update(0, SITE_PER_PAGE - 1)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("查詢失敗", "$e")
            }
        })
    }

    private fun update(begin: Int, end: Int) {

        println("list size: ${list.count()}, begin : ${begin}, end : ${end}")

        var listRange = list.slice(begin..end)

        // 案場狀態
        images.forEachIndexed lit_images@{ index, imageButton ->

            imageButton.setImageResource(R.drawable.hi_blank)
            imageButton.setBackgroundResource(R.drawable.hiblank_background_color)

            if (index > listRange.count() - 1) return@lit_images
            when (listRange[index].nSHI) {
                1 -> {
                    imageButton.setImageResource(R.drawable.hi_good)
                    imageButton.setBackgroundResource(R.drawable.higood_background_color)
                }
                2 -> {
                    imageButton.setImageResource(R.drawable.hi_good_new)
                    imageButton.setBackgroundResource(R.drawable.higood_background_color)
                }
                -3 -> {
                    imageButton.setImageResource(R.drawable.hi_alert)
                    imageButton.setBackgroundResource(R.drawable.hialert_background_color)
                }
                -4 -> {
                    imageButton.setImageResource(R.drawable.hi_alert_new)
                    imageButton.setBackgroundResource(R.drawable.hialert_background_color)
                }
                -5 -> {
                    imageButton.setImageResource(R.drawable.hi_bad)
                    imageButton.setBackgroundResource(R.drawable.hibad_background_color)
                }
                -6 -> {
                    imageButton.setImageResource(R.drawable.hi_bad_new)
                    imageButton.setBackgroundResource(R.drawable.hibad_background_color)
                }
                0 -> {
                    imageButton.setImageResource(R.drawable.hi_skip)
                    imageButton.setBackgroundResource(R.drawable.hiskip_background_color)
                }
                -1 -> {
                    imageButton.setImageResource(R.drawable.hi_err)
                    imageButton.setBackgroundResource(R.drawable.hierr_background_color)
                }
                3 -> {
                    imageButton.setImageResource(R.drawable.hi_sleep)
                    imageButton.setBackgroundResource(R.drawable.hisleep_background_color)
                }
            }
        }

        // 案場名稱
        tvs.forEachIndexed lit_tvs@{ index, textView ->

            textView.text = ""

            if (index > listRange.count() - 1) return@lit_tvs
            textView.text = listRange[index].sSite_Name
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //println("onTouchEvent : ${event}")
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDown(event: MotionEvent): Boolean {
        //println( "onDown: $event")
        return true
    }

    override fun onFling(
        event1: MotionEvent?,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val minMove = 120     //最小滑動距離
        val minVelocity = 0    //最小滑動速度
        val beginX = event1?.x
        val endX = event2.x
        val beginY = event1?.y
        val endY = event2.y

        /* 跟ScrollView的上下滑事件衝到，滑到行為怪怪的
        if (beginX - endX > minMove && abs(velocityX) > minVelocity) {
            nextPage()
        } else if (endX - beginX > minMove && Math.abs(velocityX) > minVelocity) {
            prePage()
        } else if (beginY - endY > minMove && Math.abs(velocityY) > minVelocity) {
            //println("上滑")
        } else if (endY - beginY > minMove && Math.abs(velocityY) > minVelocity) {
            //println("下滑")
        }
         */
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        //println("onLongPress: $event")
    }

    override fun onScroll(
        event1: MotionEvent?,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //println("onScroll: $event1 $event2")
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        //println("onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        //println("onSingleTapUp: $event")
        return true
    }
}