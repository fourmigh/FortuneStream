package org.caojun.fortunestream

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.caojun.fortunestream.room.Account
import org.caojun.fortunestream.room.Date
import org.caojun.fortunestream.room.Fortune
import org.caojun.fortunestream.room.FortuneDatabase
import org.caojun.utils.TimeUtils
import org.caojun.utils.CashierInputFilter
import org.caojun.utils.DigitUtils
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    private var isReadData = false
    private val tableRows = arrayListOf<TableRow>()
    private val hmRow = HashMap<String, Int>()
    private val hmColumn = HashMap<String, Int>()
    private val accounts = ArrayList<Account>()
    private val dates= ArrayList<Date>()
    private val fortunes = ArrayList<Fortune>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tableRows.add(tableRow0)
        tableRows.add(tableRow1)

        btnAddAccount.setOnClickListener {

            linearLayout.removeView(btnAddAccount)
            linearLayout.addView(getAccountEditText())
            linearLayout.addView(btnAddAccount)

            val tableRow = TableRow(this@MainActivity)
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

            tableRows[1].addView(getBtnTotal(column))

            checkBtnAddData()

            addEditText()
        }

        doRead()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        val id = item.itemId
//
//        if (id == R.id.action_save) {
//            doSave()
//            return true
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

    override fun onPause() {
        super.onPause()
        doSave()
    }

    private fun doSave() {
        doAsync {
            val row = linearLayout.childCount - 3
            if (row < 1) {
                return@doAsync
            }
            val column = tableRows[0].childCount - 1
            if (column < 1) {
                return@doAsync
            }
            for (i in 0 until row) {
                val indexRow = i + 2
                val etAccount = linearLayout.getChildAt(indexRow)
                if (etAccount is EditText) {
                    val account = Account(etAccount.text.toString())
                    FortuneDatabase.getDatabase(this@MainActivity).getAccountDao().insert(account)

                    for (j in 0 until column) {
                        val tvDate = tableRows[0].getChildAt(j)
                        if (tvDate is TextView) {
                            val date = Date(tvDate.text.toString())
                            FortuneDatabase.getDatabase(this@MainActivity).getDateDao().insert(date)

                            val etFortune = tableRows[indexRow].getChildAt(j)
                            if (etFortune is EditText) {
                                val fortune = Fortune(account.account, date.date, etFortune.text.toString().toDouble())
                                FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().insert(fortune)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun doRead() {
        doAsync {
            accounts.clear()
            dates.clear()
            fortunes.clear()

            accounts.addAll(FortuneDatabase.getDatabase(this@MainActivity).getAccountDao().queryAll())
            dates.addAll(FortuneDatabase.getDatabase(this@MainActivity).getDateDao().queryAll())
            fortunes.addAll(FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().queryAll())

            uiThread {
                isReadData = true
                hmRow.clear()
                hmColumn.clear()

                for (i in accounts.indices) {

                    btnAddAccount.callOnClick()

                    val etAccount = linearLayout.getChildAt(linearLayout.childCount - 2)
                    if (etAccount is EditText) {
                        etAccount.setText(accounts[i].account)
                    }
                }

                for (i in dates.indices) {
                    btnAddData.callOnClick()

                    val tvDate = tableRows[0].getChildAt(tableRows[0].childCount - 2)
                    if (tvDate is TextView) {
                        tvDate.text = dates[i].date
                    }
                }

                for (i in fortunes.indices) {
                    val account = fortunes[i].account
                    val date = fortunes[i].date
                    val fortune = fortunes[i].fortune

                    val row = searchRow(account)
                    if (row == -1) {
                        continue
                    }
                    val column = searchColumn(date)
                    if (column == -1) {
                        continue
                    }
                    val etFortune = tableRows[row].getChildAt(column)
                    if (etFortune is EditText) {
                        etFortune.setText(DigitUtils.getRound(fortune, 2))
                    }
                }

                isReadData = false

                checkBtnAddData()
            }
        }
    }

    private fun searchRow(account: String): Int {
        val row = hmRow[account]
        if (row != null) {
            return row
        }
        for (i in 2 until  linearLayout.childCount - 1) {
            val etAccount = linearLayout.getChildAt(i)
            if (etAccount is EditText && etAccount.text.toString() == account) {
                hmRow[account] = i
                return i
            }
        }
        return -1
    }

    private fun searchColumn(date: String): Int {
        val column = hmColumn[date]
        if (column != null) {
            return column
        }
        for (i in 0 until tableRows[0].childCount - 1) {
            val tvDate = tableRows[0].getChildAt(i)
            if (tvDate is TextView && tvDate.text.toString() == date) {
                hmColumn[date] = i
                return i
            }
        }
        return -1
    }

    private fun checkBtnAddData() {
        if (isReadData) {
            btnAddData.isEnabled = true
            return
        }
        val textView = tableRows[0].getChildAt(tableRows[0].childCount - 2)
        val today = TimeUtils.getTime("yyyy-MM-dd")
        btnAddData.isEnabled = !(textView is TextView && textView.text.toString() == today)
    }

    private fun addEditText() {
        val nColumn = tableRows[0].childCount - 1//总列数
        for (i in 2 until linearLayout.childCount - 1) {
            val childCount = tableRows[i].childCount
            val column = nColumn - childCount
            for (j in 0 until column) {
                tableRows[i].addView(getAmountEditText(tableRows[i].childCount, i))
            }
        }
    }

    private fun getAmountEditText(column: Int, row: Int): EditText {
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
                for (i in 2 until tableRows.size) {
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
                val button = tableRows[1].getChildAt(column)
                if (button is Button) {
                    button.text = DigitUtils.getRound(total, 2)
                    checkTotaleButtonColor(button, column)
                }

                val lastValue = getLastAmount(editText, column - 1, row)
                val value = getValue(editText)

                when {
                    value > lastValue -> editText.setTextColor(Color.RED)
                    value < lastValue -> editText.setTextColor(Color.GREEN)
                    else -> editText.setTextColor(Color.BLACK)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        return editText
    }

    private fun checkTotaleButtonColor(button: Button, column: Int) {
        val lastValue = getLastTotal(button, column - 1)
        val value = getValue(button)

        when {
            value > lastValue -> button.setTextColor(Color.RED)
            value < lastValue -> button.setTextColor(Color.GREEN)
            else -> button.setTextColor(Color.BLACK)
        }
    }

    private fun getAccountEditText(): EditText {
        val editText = EditText(this)
        editText.maxLines = 1
        return editText
    }

    private fun getTextText(): TextView {
        val textView = TextView(this)
        textView.gravity = Gravity.CENTER
        textView.setPadding(4, 4, 4, 4)
        return textView
    }

    private fun getBtnTotal(column: Int): Button {
        val button = Button(this)
        //总计
        button.setOnClickListener {
            doDeleteColumn(column)
        }
        return button
    }

    private fun doDeleteColumn(column: Int) {
        alert {
            title = getString(R.string.alert_delete_date, dates[column].date)
            positiveButton(android.R.string.ok) {
                deleteColumn(column)
            }
            negativeButton(android.R.string.cancel) {}
        }.show()
    }

    private fun deleteColumn(column: Int) {
        doAsync {
            FortuneDatabase.getDatabase(this@MainActivity).getDateDao().delete(dates[column])
            for (i in fortunes.indices) {
                if (fortunes[i].date == dates[column].date) {
                    FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().delete(fortunes[i])
                }
            }
            uiThread {
                for (i in tableRows.indices) {
                    tableRows[i].removeViewAt(column)
                }
            }
        }
    }

    private fun getLastBtnTotal(column: Int): Button? {
        if (column < 0) {
            return null
        }
        val button = tableRows[1].getChildAt(column)
        if (button is Button) {
            return button
        }
        return null
    }

    private fun getLastTotal(button: Button, column: Int): Double {
        val value = getValue(button)
        val lastEditText = getLastBtnTotal(column)
        return if (lastEditText == null) value else getValue(lastEditText)
    }

    private fun getLastAmountEditText(column: Int, row: Int): EditText? {
        if (column < 0) {
            return null
        }
        val editText = tableRows[row].getChildAt(column)
        if (editText is EditText) {
            return editText
        }
        return null
    }

    private fun getLastAmount(editText: EditText, column: Int, row: Int): Double {
        val value = getValue(editText)
        val lastEditText = getLastAmountEditText(column, row)
        return if (lastEditText == null) value else getValue(lastEditText)
    }

    private fun getValue(textView: TextView): Double {
        val value = textView.text.toString()
        if (TextUtils.isEmpty(value)) {
            return 0.toDouble()
        }
        return value.toDouble()
    }
}
