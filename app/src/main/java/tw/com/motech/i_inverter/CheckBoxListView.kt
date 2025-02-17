package tw.com.motech.i_inverter_idv

import android.content.Context
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SearchView
import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class CheckBoxListView @JvmOverloads constructor(
    parent: View,
    private val options: List<String> = emptyList(),
    private val onSelectionChanged: (List<String>) -> Unit
) : LinearLayout(parent.context) {

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.checkbox_list_layout, this, true)

        val searchView = findViewById<SearchView>(R.id.searchView)
        val checkboxContainer = findViewById<LinearLayout>(R.id.checkboxContainer)
        val selectAllButton = findViewById<Button>(R.id.buttonSelectAll)

        // 這裡可以添加初始化和事件處理邏輯

        // 設定搜尋功能
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 在這裡實現 CheckBox 的篩選邏輯
                filterCheckBoxOptions(checkboxContainer, newText.orEmpty())
                return true
            }
        })

        // 設定全選功能
        selectAllButton.setOnClickListener {
            handleSelectAllButtonClick()
        }

        // 動態生成 CheckBox
        for (option in options) {
            val checkBox = CheckBox(context)
            checkBox.text = option
            checkboxContainer.addView(checkBox)
        }
    }

    private fun filterCheckBoxOptions(container: LinearLayout, query: String) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is CheckBox) {
                val optionText = child.text.toString()
                if (optionText.contains(query, true)) {
                    child.visibility = VISIBLE
                } else {
                    child.visibility = GONE
                }
            }
        }
    }

    fun showCheckBoxListDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Options")
        builder.setView(this)
        builder.setPositiveButton("OK") { dialog, which ->
            // 在這裡處理選擇的結果
            handleCheckBoxSelection(findViewById(R.id.checkboxContainer))
        }
        builder.setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun handleCheckBoxSelection(container: LinearLayout) {
        val selectedOptions = mutableListOf<String>()

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is CheckBox && child.isChecked) {
                selectedOptions.add(child.text.toString())
            }
        }

        onSelectionChanged(selectedOptions)
    }

    private fun handleSelectAllButtonClick() {
        val container = findViewById<LinearLayout>(R.id.checkboxContainer)
        val selectedOptions = mutableListOf<String>()

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is CheckBox) {
                child.isChecked = true
                selectedOptions.add(child.text.toString())
            }
        }
    }
}
