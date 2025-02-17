package tw.com.motech.i_inverter_idv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

class TaiwanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_taiwan)

        val btn_north = findViewById<Button>(R.id.btn_north)
        val btn_center = findViewById<Button>(R.id.btn_center)
        val btn_south = findViewById<Button>(R.id.btn_south)

        /*
        sSiteType是代表北中南的欄位
        1 : 北部
        2 : 中部
        3 : 南部
        4 : 東部
        NULL : Other
        */

        val b = Bundle()
        btn_north.setOnClickListener{
            b.putString("sSiteType", "1")
            GoToSiteActivity(b)
        }
        btn_center.setOnClickListener{
            b.putString("sSiteType", "2")
            GoToSiteActivity(b)
        }
        btn_south.setOnClickListener{
            b.putString("sSiteType", "3")
            GoToSiteActivity(b)
        }
    }

    private fun GoToSiteActivity(b: Bundle) {
        val intent = Intent(this, SiteActivity::class.java)
        intent.putExtras(b)
        startActivity(intent)
    }
}