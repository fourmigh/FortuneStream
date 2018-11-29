package org.caojun.fortunestream

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
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
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    private var isReadData = false
    private val tableRows = arrayListOf<TableRow>()
//    private val hmRow = HashMap<String, Int>()
//    private val hmColumn = HashMap<String, Int>()
    private val accounts = ArrayList<Account>()
    private val dates= ArrayList<Date>()
    private val fortunes = ArrayList<Fortune>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //禁止屏幕截图
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContentView(R.layout.activity_main)

        tableRows.add(tableRow0)
        tableRows.add(tableRow1)

        btnAddAccount.setOnClickListener {
            doAddAccount()
        }

        btnAddData.setOnClickListener {
            doAddDate()
        }

        doRead()
    }
    
    private fun doAddAccount() {
        val tvAccount = newAccountTextView(linearLayout.childCount - 1)
        linearLayout.addView(tvAccount, linearLayout.childCount - 1)

        val tableRow = TableRow(this)
        tableLayout.addView(tableRow)
        tableRows.add(tableRow)

        checkBtnAddData()

        addEditText()

        if (!isReadData) {
            tvAccount.requestFocus()
        }

        checkBtnAddAccount()
    }
    
    private fun doAddDate() {

        val date = if (isReadData) dates[tableRows[1].childCount].date else newTodayDate()
        val btnDate = newDateButton(date)
        //第一行日期
        tableRows[0].addView(btnDate, 0)

        //第二行总计
        tableRows[1].addView(newTotalButton(btnDate.text.toString()), 0)

        checkBtnAddData()

        addEditText()

        if (tableRows[0].childCount > 1) {
            val btnLastDate = tableRows[0].getChildAt(1)
            if (btnLastDate is Button) {
                btnLastDate.callOnClick()
            }
        }
    }

    private fun newTodayDate(): String {
        return TimeUtils.getTime("yyyy-MM-dd")
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
            val alAccount = ArrayList<String>()
            val alDate = ArrayList<String>()
            for (i in 0 until row) {
                val indexRow = i + 2
                val tvAccount = linearLayout.getChildAt(indexRow)
                if (tvAccount is TextView) {
                    val nameAccount = tvAccount.text.toString()
                    if (TextUtils.isEmpty(nameAccount) || nameAccount in alAccount) {
                        continue
                    }
                    val account = Account(nameAccount)
                    FortuneDatabase.getDatabase(this@MainActivity).getAccountDao().insert(account)
                    alAccount.add(nameAccount)

                    for (j in column - 1 downTo 0) {
                        val btnDate = tableRows[0].getChildAt(j)
                        if (btnDate is Button) {
                            val date = Date(btnDate.text.toString())
                            if (date.date !in alDate) {
                                FortuneDatabase.getDatabase(this@MainActivity).getDateDao().insert(date)
                                alDate.add(date.date)
                            }

                            val etFortune = tableRows[indexRow].getChildAt(j)
                            if (etFortune is EditText) {
                                val text = etFortune.text.toString()
                                val value = if (TextUtils.isEmpty(text)) 0.toDouble() else text.toDouble()
                                val fortune = Fortune(account.account, date.date, value)
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
//                hmRow.clear()
//                hmColumn.clear()

                for (i in accounts.indices) {

                    btnAddAccount.callOnClick()

                    val tvAccount = linearLayout.getChildAt(linearLayout.childCount - 2)
                    if (tvAccount is TextView) {
                        tvAccount.text = accounts[i].account
                    }
                }

                for (i in dates.indices) {
                    btnAddData.callOnClick()
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
//        val row = hmRow[account]
//        if (row != null) {
//            return row
//        }
        for (i in 2 until  linearLayout.childCount - 1) {
            val tvAccount = linearLayout.getChildAt(i)
            if (tvAccount is TextView && tvAccount.text.toString() == account) {
//                hmRow[account] = i
                return i
            }
        }
        return -1
    }

    private fun searchColumn(date: String): Int {
//        val column = hmColumn[date]
//        if (column != null) {
//            return column
//        }
        for (i in 0 until tableRows[0].childCount) {
            val btnDate = tableRows[0].getChildAt(i)
            if (btnDate is Button && btnDate.text.toString() == date) {
//                hmColumn[date] = i
                return i
            }
        }
        return -1
    }

    private fun checkBtnAddAccount() {
        for (i in 2 until linearLayout.childCount - 1) {
            val tvAccount = linearLayout.getChildAt(i)
            if (tvAccount is TextView && TextUtils.isEmpty(tvAccount.text.toString())) {
                btnAddAccount.isEnabled = false
                return
            }
        }
        btnAddAccount.isEnabled = true
    }

    private fun checkBtnAddData() {
        if (isReadData) {
            btnAddData.isEnabled = true
            return
        }
        val textView = tableRows[0].getChildAt(0)
        val today = newTodayDate()
        btnAddData.isEnabled = !(textView is TextView && textView.text.toString() == today)
    }

    private fun addEditText() {
        val nColumn = tableRows[0].childCount//总列数
        for (i in 2 until tableRows.size) {
            val childCount = tableRows[i].childCount
            val column = nColumn - childCount
            for (j in 0 until column) {
                val index = tableRows[0].childCount - tableRows[i].childCount - 1
                val btnDate = tableRows[0].getChildAt(index) as Button
                tableRows[i].addView(newAmountEditText(btnDate.text.toString(), i), 0)
            }
        }
    }

    private fun newAmountEditText(date: String, row: Int): EditText {
        val editText = EditText(this)
        editText.maxLines = 1
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val filters = arrayOf<InputFilter>(CashierInputFilter())
        editText.filters = filters

        //column列号
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //实时计算总计
                calculateTotal(editText, s, date, row)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val text = editText.text.toString()
                if (!TextUtils.isEmpty(text)) {
                    val value = text.toDouble()
                    if (value == 0.toDouble()) {
                        editText.text = null
                    } else {
                        editText.setSelection(text.length)
                    }
                }
            }
        }

        editText.setOnLongClickListener {
            //显示比前一天增加或减少金额
            val lastValue = getLastAmount(editText, date, row)
            val value = getValue(editText)
            showDifferenceValue(value, lastValue)
            true
        }

        editText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val column = searchColumn(date)
                if (column != -1 && row + 1 < tableRows.size) {
                    val etAmount = tableRows[row + 1].getChildAt(column)
                    if (etAmount is EditText) {
                        etAmount.requestFocus()
                    }
                } else {
                    val lastEditText = getLastAmountEditText(date, row)
                    if (lastEditText is EditText) {
                        lastEditText.requestFocus()
                    }
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        return editText
    }

    private fun calculateTotal(editText: EditText, s: Editable?, date: String, row: Int) {
        var total = 0.0
        val column = searchColumn(date)
        if (column >= 0) {
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
        }
        if (!TextUtils.isEmpty(s)) {
            total += s.toString().toDouble()
        }
        val button = tableRows[1].getChildAt(column)
        if (button is Button) {
            button.text = DigitUtils.getRound(total, 2)
            checkTotalButtonColor(button, date)
        }

        val lastValue = getLastAmount(editText, date, row)
        val value = getValue(editText)

        when {
            value > lastValue -> editText.setTextColor(Color.RED)
            value < lastValue -> editText.setTextColor(Color.GREEN)
            else -> editText.setTextColor(Color.BLACK)
        }
    }

    private fun showDifferenceValue(value: Double, lastValue: Double) {
        val sign = if (value >= lastValue) "+" else "-"
        val absolute = DigitUtils.getRound(Math.abs(value - lastValue), 2)
        toast("$sign $absolute")
    }

    private fun checkTotalButtonColor(button: Button, dateToday: String) {
        val lastValue = getLastTotal(button, dateToday)
        val value = getValue(button)

        when {
            value > lastValue -> button.setTextColor(Color.RED)
            value < lastValue -> button.setTextColor(Color.GREEN)
            else -> button.setTextColor(Color.BLACK)
        }
    }

    private fun newAccountTextView(row: Int): TextView {
        val editText = EditText(this)
        editText.maxLines = 1
        editText.setOnLongClickListener {
            doDeleteRow(editText.text.toString())
            true
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val account = editText.text.toString()
                for (i in 2 until linearLayout.childCount) {
                    val tvAccount = linearLayout.getChildAt(i)
                    if (tvAccount == editText) {
                        continue
                    }
                    if (tvAccount is TextView && tvAccount.text.toString() == account) {
                        alert {
                            messageResource = R.string.same_account
                            positiveButton(android.R.string.ok) {
                                editText.text = null
                                editText.requestFocus()
                            }
                        }.show()
                        return@setOnFocusChangeListener
                    }
                }
            }
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkBtnAddAccount()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        editText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (row + 1 < linearLayout.childCount - 1) {
                    val etAccount = linearLayout.getChildAt(row + 1)
                    if (etAccount is EditText) {
                        etAccount.requestFocus()
                    }
                } else {
                    val etAmount = tableRows[row].getChildAt(0)
                    if (etAmount is EditText) {
                        etAmount.requestFocus()
                    }
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        return editText
    }

    private fun doDeleteRow(account: String) {
        alert {
            title = getString(R.string.alert_delete_data, account)
            positiveButton(android.R.string.ok) {
                deleteRow(account)
            }
            negativeButton(android.R.string.cancel) {}
        }.show()
    }

    private fun deleteRow(account: String) {
        doAsync {
            val row = searchRow(account)
            FortuneDatabase.getDatabase(this@MainActivity).getAccountDao().delete(accounts[row - 2])
            for (i in fortunes.indices) {
                if (fortunes[i].account == account) {
                    FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().delete(fortunes[i])
                }
            }
            uiThread {
                linearLayout.removeViewAt(row)
                tableRows[row].removeAllViews()
                tableRows.removeAt(row)
            }
        }
    }

    private fun newDateButton(date: String): Button {
        val button = Button(this)
        button.text = date
        button.setOnClickListener {
            val column = searchColumn(date)
            if (column != -1) {
                var isEnabled: Boolean? = null
                for (i in 2 until tableRows.size) {
                    val etAmount = tableRows[i].getChildAt(column)
                    if (etAmount is EditText) {
                        if (isEnabled == null) {
                            isEnabled = !etAmount.isEnabled
                        }
                        etAmount.isEnabled = isEnabled
                    }
                }
            }
        }
        return button
    }

    private fun newTotalButton(date: String): Button {
        val button = Button(this)
        button.setOnLongClickListener {
            doDeleteColumn(date)
            true
        }
        button.setOnClickListener {
            //显示比前一天增加或减少金额
            val lastValue = getLastTotal(button, date)
            val value = getValue(button)
            showDifferenceValue(value, lastValue)
        }
        return button
    }

    private fun doDeleteColumn(date: String) {
        alert {
            title = getString(R.string.alert_delete_data, date)
            positiveButton(android.R.string.ok) {
                deleteColumn(date)
            }
            negativeButton(android.R.string.cancel) {}
        }.show()
    }

    private fun deleteColumn(date: String) {
        doAsync {
            var column = 0
            for (i in dates.indices) {
                if (dates[i].date == date) {
                    FortuneDatabase.getDatabase(this@MainActivity).getDateDao().delete(dates[i])
                    column = i
                    break
                }
            }
            for (i in fortunes.indices) {
                if (fortunes[i].date == date) {
                    FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().delete(fortunes[i])
                }
            }
            uiThread {
                for (i in tableRows.indices) {
                    tableRows[i].removeViewAt(column)
                }
                checkBtnAddData()
            }
        }
    }

    private fun getLastTotal(dateToday: String): TextView? {
        val column = searchColumn(dateToday)
        if (column != -1 && column < tableRows[1].childCount - 1) {
            val tvTotal = tableRows[1].getChildAt(column + 1)
            if (tvTotal is TextView) {
                return tvTotal
            }
        }
        return null
    }

    private fun getLastTotal(button: Button, dateToday: String): Double {
        val value = getValue(button)
        val lastTextView = getLastTotal(dateToday)
        return if (lastTextView == null) value else getValue(lastTextView)
    }

    private fun getLastAmountEditText(dateToday: String, row: Int): EditText? {
        val column = searchColumn(dateToday)
        if (column != -1 && column < tableRows[1].childCount - 1) {
            val etAmount = tableRows[row].getChildAt(column + 1)
            if (etAmount is EditText) {
                return etAmount
            }
        }
        return null
    }

    private fun getLastAmount(editText: EditText, dateToday: String, row: Int): Double {
        val value = getValue(editText)
        val lastEditText = getLastAmountEditText(dateToday, row)
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
