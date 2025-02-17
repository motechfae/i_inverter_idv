package tw.com.motech.i_inverter_idv

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class InverterListAdapterActivity(private val inverterresults: List<InverterResult>) : RecyclerView.Adapter<InverterListAdapterActivity.ViewHolder>() {
    class ViewHolder(v: View):RecyclerView.ViewHolder(v){
        val img_connect = v.findViewById<ImageView>(R.id.img_Connect)
        val txt_invsn = v.findViewById<TextView>(R.id.txt_InvSn)
        val txt_realea = v.findViewById<TextView>(R.id.txt_RelEa)
        val txt_invuptime = v.findViewById<TextView>(R.id.txt_InvUpTime)
        val txt_errcode = v.findViewById<TextView>(R.id.txt_ErrCode)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.activity_inverter_list_adapter_row,
            viewGroup,
            false
        )
        return  ViewHolder(v)
    }

    override fun getItemCount() = inverterresults.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (inverterresults[position].ConChk) {
            1 -> {
                holder.img_connect.setImageResource(R.drawable.connect)
            }
            else -> {
                holder.img_connect.setImageResource(R.drawable.disconnect)
            }
        }
        holder.txt_invsn.text = inverterresults[position].sSNID + "(${inverterresults[position].nRS485ID})"
        holder.txt_realea.text = inverterresults[position].nEa.toString() + " kw"
        /*
        val simpleDateFormat_parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val simpleDateFormat_formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        holder.txt_invuptime.text = simpleDateFormat_formatter.format(simpleDateFormat_parser.parse(inverterresults[position].dCreat_Time))
        */
        holder.txt_invuptime.text = inverterresults[position].dCreat_Time
        //holder.txt_invuptime.text = inverterresults[position].dCreat_Time
        if (inverterresults[position].sErrCode == ""){
            holder.txt_errcode.text = "No Error"
            holder.txt_errcode.setTextColor(Color.BLUE)
        }
        else{
            holder.txt_errcode.text = inverterresults[position].sErrCode
            holder.txt_errcode.setTextColor(Color.RED)
        }

    }
}