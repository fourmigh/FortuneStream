package org.caojun.fortunestream

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.caojun.utils.TimeUtils
import org.caojun.utils.CashierInputFilter


class MainActivity : AppCompatActivity() {

    private val tableRows = arrayListOf<TableRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tableRows.add(tableRow0)
        tableRows.add(tableRow1)
        tableRows.add(tableRow2)

        btnAddAccount.setOnClickListener {

            tableRows[tableRows.size - 1].removeAllViews()

            val tableRow = TableRow(this@MainActivity)
            tableRow.addView(btnAddAccount)
            tableLayout.addView(tableRow)

            tableRows.add(tableRow)

            checkBtnAddData()

            addEditText()
        }

        btnAddData.setOnClickListener {

            tableRows[0].removeView(btnAddData)

            val column = tableRows[0].childCount

            val tvDate = getTextText()
            tvDate.text = TimeUtils.getTime("yyyy-MM-dd")
            tableRows[0].addView(tvDate)

            tableRows[0].addView(btnAddData)

            tableRows[1].addView(getTextText(column))

            checkBtnAddData()

            addEditText()
        }
    }

    private fun checkBtnAddData() {
        val textView = tableRows[0].getChildAt(tableRows[0].childCount - 2)
        val today = TimeUtils.getTime("yyyy-MM-dd")
//        btnAddData.isEnabled = !(textView is TextView && textView.text.toString() == today)
        btnAddData.isEnabled = true
    }

    private fun addEditText() {
        val nColumn = tableRows[0].childCount - 1
        for (i in 2 until tableRows.size - 1) {
            val childCount = tableRows[i].childCount
            val column = nColumn - childCount
            for (j in 0 until column) {
                if (childCount == 0 && j == 0) {
                    //第一列是账户名称
                    tableRows[i].addView(getEditText())
                } else {
                    //只能输入金额
                    tableRows[i].addView(getAmountEditText(tableRows[i].childCount))
                }
            }
        }
    }

    private fun getAmountEditText(column: Int): EditText {
        val editText = EditText(this)
        editText.maxLines = 1
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val filters = arrayOf<InputFilter>(CashierInputFilter())
        editText.filters = filters

        //column列号
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //实时计算总计
                var total = 0.0
                for (i in 2 until tableRows.size - 1) {
                    val et = tableRows[i].getChildAt(column)
                    if (et is EditText && et != editText) {
                        val value = et.text.toString()
                        if (TextUtils.isEmpty(value)) {
                            continue
                        }
                        total += value.toDouble()
                    }
                }
                if (!TextUtils.isEmpty(s)) {
                    total += s.toString().toDouble()
                }
                val textView = tableRows[1].getChildAt(column)
                if (textView is TextView) {
                    textView.text = total.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        return editText
    }

    private fun getEditText(): EditText {
        val editText = EditText(this)
        editText.maxLines = 1
        return editText
    }

    private fun getTextText(column: Int? = null): TextView {
        val textView = TextView(this)
        textView.setPadding(4, 4, 4, 4)
        if (column != null) {
            //总计
            textView.setOnClickListener {
                var total = 0.0
                for (i in 2 until tableRows.size - 1) {
                    val editText = tableRows[i].getChildAt(column)
                    if (editText is EditText) {
                        val value = editText.text.toString()
                        if (TextUtils.isEmpty(value)) {
                            continue
                        }
                        total += value.toDouble()
                    }
                }
                textView.text = total.toString()
            }
        }
        return textView
    }
}
