package org.caojun.fortunestream

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.*
import android.view.*
import kotlinx.android.synthetic.main.activity_main.*
import org.caojun.utils.CashierInputFilter
import org.caojun.utils.TimeUtils
import android.widget.*
import org.caojun.fortunestream.room.Account
import org.caojun.fortunestream.room.Date
import org.caojun.fortunestream.room.Fortune
import org.caojun.fortunestream.room.FortuneDatabase
import org.caojun.utils.DigitUtils
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    private val tableRows = arrayListOf<TableRow>()
    private var isReadData = false
    private val accounts = ArrayList<Account>()
    private val dates = ArrayList<Date>()
    private val fortunes = ArrayList<Fortune>()
    private var widthStandard = 0
    private var heightStandard = 0
    private var isSaveMenuEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //禁止屏幕截图
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContentView(R.layout.activity_main)
        svData.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            svAccount.smoothScrollTo(scrollX, scrollY)
        }

        svAccount.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            svData.smoothScrollTo(scrollX, scrollY)
        }

        hsvData.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            hsvDate.smoothScrollTo(scrollX, scrollY)
            hsvTotal.smoothScrollTo(scrollX, scrollY)
        }

        hsvDate.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            hsvData.smoothScrollTo(scrollX, scrollY)
            hsvTotal.smoothScrollTo(scrollX, scrollY)
        }

        hsvTotal.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            hsvDate.smoothScrollTo(scrollX, scrollY)
            hsvData.smoothScrollTo(scrollX, scrollY)
        }

        btnAddDate.setOnClickListener {
            doAddDate()
        }

        btnTotal.setOnClickListener {
            doBtnTotal()
        }

        btnAddAccount.setOnClickListener {
            doAddAccount()
        }

        doRead()
    }

    private fun updateSaveMenu(isEnabled: Boolean) {
        isSaveMenuEnabled = isEnabled
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)

        val saveMenu = menu.add(0, R.id.action_save, 0, R.string.save)
        saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        saveMenu.isEnabled = isSaveMenuEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_save) {
            doSave()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun doSave() {
        doAsync {
            val row = llAccount.childCount - 1
            if (row < 1) {
                return@doAsync
            }
            val column = llDate.childCount
            if (column < 1) {
                return@doAsync
            }
            val alAccount = ArrayList<String>()
            val alDate = ArrayList<String>()
            for (i in 0 until row) {
                val tvAccount = llAccount.getChildAt(i)
                if (tvAccount is TextView) {
                    val nameAccount = tvAccount.text.toString()
                    if (TextUtils.isEmpty(nameAccount) || nameAccount in alAccount) {
                        continue
                    }
                    val account = Account(nameAccount)
                    FortuneDatabase.getDatabase(this@MainActivity).getAccountDao().insert(account)
                    alAccount.add(nameAccount)

                    for (j in column - 1 downTo 0) {
                        val btnDate = llDate.getChildAt(j)
                        if (btnDate is Button) {
                            val date = Date(btnDate.text.toString())
                            if (date.date !in alDate) {
                                FortuneDatabase.getDatabase(this@MainActivity).getDateDao().insert(date)
                                alDate.add(date.date)
                            }

                            val etFortune = tableRows[i].getChildAt(j)
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
            updateSaveMenu(false)
        }
    }

    private fun searchRow(account: String): Int {
        for (i in 0 until llAccount.childCount - 1) {
            val tvAccount = llAccount.getChildAt(i)
            if (tvAccount is TextView && tvAccount.text.toString() == account) {
                return i
            }
        }
        return -1
    }

    private fun searchColumn(date: String): Int {
        for (i in 0 until llDate.childCount) {
            val btnDate = llDate.getChildAt(i)
            if (btnDate is Button && btnDate.text.toString() == date) {
                return i
            }
        }
        return -1
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

                for (i in accounts.indices) {

                    btnAddAccount.callOnClick()

                    val tvAccount = llAccount.getChildAt(llAccount.childCount - 2)
                    if (tvAccount is TextView) {
                        tvAccount.text = accounts[i].account
                    }
                }

                for (i in dates.indices) {
                    btnAddDate.callOnClick()
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

                doBtnTotal()
            }
        }
    }

    private fun checkBtnAddAccount() {
        for (i in 0 until llAccount.childCount - 1) {
            val tvAccount = llAccount.getChildAt(i)
            if (tvAccount is TextView && TextUtils.isEmpty(tvAccount.text.toString())) {
                btnAddAccount.isEnabled = false
                return
            }
        }
        btnAddAccount.isEnabled = true
    }

    private fun doBtnTotal() {
        if (btnAddAccount.isEnabled) {
            btnAddAccount.isEnabled = false
        } else {
            checkBtnAddAccount()
        }
        var isEnabled: Boolean? = null
        for (i in 0 until llAccount.childCount - 1) {
            val etAccount = llAccount.getChildAt(i)
            if (etAccount is EditText) {
                if (isEnabled == null) {
                    isEnabled = !etAccount.isEnabled
                }
                etAccount.isEnabled = isEnabled
            }
        }
    }

    private fun newTodayDate(): String {
        return TimeUtils.getTime("yyyy-MM-dd")
    }

    private fun newDateButton(date: String): Button {
        val button = Button(this)
        button.text = date
        button.setOnClickListener {
            val column = searchColumn(date)
            if (column != -1) {
                var isEnabled: Boolean? = null
                for (i in 0 until tableRows.size) {
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

    private fun checkBtnAddData() {
        if (isReadData) {
            btnAddDate.isEnabled = true
            return
        }
        val btnDate = llDate.getChildAt(0)
        val today = newTodayDate()
        btnAddDate.isEnabled = !(btnDate is Button && btnDate.text.toString() == today)
    }

    private fun deleteColumn(date: String) {
        doAsync {
            for (i in dates.indices) {
                if (dates[i].date == date) {
                    FortuneDatabase.getDatabase(this@MainActivity).getDateDao().delete(dates[i])
                    break
                }
            }
            for (i in fortunes.indices) {
                if (fortunes[i].date == date) {
                    FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().delete(fortunes[i])
                }
            }

            val column = searchColumn(date)
            uiThread {
                for (i in tableRows.indices) {
                    tableRows[i].removeViewAt(column)
                }
                llDate.removeViewAt(column)
                llTotal.removeViewAt(column)
                trStandard.removeViewAt(column)
                checkBtnAddData()
            }
        }
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

    private fun doAddDate() {
        val date = if (isReadData) dates[llDate.childCount].date else newTodayDate()

        val btnDate = newDateButton(date)
        btnDate.text = date
        llDate.addView(btnDate, 0)
        setView(btnDate, btnStandard)

        val btnTotal = newTotalButton(date)
        llTotal.addView(btnTotal, 0)
        setView(btnTotal, btnStandard)

        if (trStandard.childCount < llTotal.childCount) {
            val btnInvisible = Button(this)
            btnInvisible.text = btnStandard.text
            btnInvisible.visibility = View.INVISIBLE
            trStandard.addView(btnInvisible)
        }

        checkBtnAddData()

        doAddAmountEditText()

        if (llDate.childCount > 1) {
            val btnLastDate = llDate.getChildAt(1)
            if (btnLastDate is Button) {
                btnLastDate.callOnClick()
            }
        }

        if (!isReadData) {
            val etFirst = tableRows[0].getChildAt(0)
            etFirst.requestFocus()
        }
    }

    private fun setView(view: View, vStandard: View) {
        if (widthStandard == 0 || heightStandard == 0) {
            vStandard.post {
                widthStandard = vStandard.width
                heightStandard = vStandard.height
                setView(view, vStandard)
            }
        } else {
            val layoutParams = view.layoutParams
            layoutParams.width = widthStandard
            layoutParams.height = heightStandard
            view.layoutParams = layoutParams
        }
    }

    private fun deleteRow(account: String) {
        doAsync {
            for (i in accounts.indices) {
                if (accounts[i].account == account) {
                    FortuneDatabase.getDatabase(this@MainActivity).getAccountDao().delete(accounts[i])
                    break
                }
            }
            for (i in fortunes.indices) {
                if (fortunes[i].account == account) {
                    FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().delete(fortunes[i])
                }
            }
            val row = searchRow(account)
            uiThread {
                llAccount.removeViewAt(row)
                tableRows[row].removeAllViews()
                tableRows.removeAt(row)
            }
        }
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

    private fun newAccountTextView(row: Int): TextView {
        val editText = EditText(this)
        editText.setSingleLine()
        editText.setOnLongClickListener {
            doDeleteRow(editText.text.toString())
            true
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val text = editText.text.toString()
                editText.setSelection(text.length)
            } else {
                val account = editText.text.toString()
                for (i in 0 until llAccount.childCount - 1) {
                    val etAccount = llAccount.getChildAt(i)
                    if (etAccount == editText) {
                        continue
                    }
                    if (etAccount is EditText && etAccount.text.toString() == account) {
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
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_UP) {
                    if (row + 1 < llAccount.childCount - 1) {
                        val etAccount = llAccount.getChildAt(row + 1)
                        if (etAccount is EditText) {
                            etAccount.requestFocus()
                        }
                    } else {
                        val etAmount = tableRows[row].getChildAt(0)
                        if (etAmount is EditText) {
                            etAmount.requestFocus()
                        }
                    }
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        return editText
    }

    private fun doAddAccount() {
        val etAccount = newAccountTextView(llAccount.childCount - 1)
        llAccount.addView(etAccount, llAccount.childCount - 1)

        val tableRow = TableRow(this)
        tableRows.add(tableRow)
        tlData.addView(tableRow, tlData.childCount - 1)

        checkBtnAddData()

        doAddAmountEditText()

        if (!isReadData) {
            etAccount.requestFocus()
        }

        checkBtnAddAccount()
    }

    private fun doAddAmountEditText() {
        val nColumn = llDate.childCount//总列数
        for (i in 0 until tableRows.size) {
            val childCount = tableRows[i].childCount
            val column = nColumn - childCount
            for (j in 0 until column) {
                val index = nColumn - tableRows[i].childCount - 1
                val btnDate = llDate.getChildAt(index) as Button
                tableRows[i].addView(newAmountEditText(btnDate.text.toString(), i), 0)
            }
        }
    }

    private fun getValue(textView: TextView): Double {
        val value = textView.text.toString()
        if (TextUtils.isEmpty(value)) {
            return 0.toDouble()
        }
        return value.toDouble()
    }

    private fun getLastTotal(dateToday: String): TextView? {
        val column = searchColumn(dateToday)
        if (column != -1 && column < llTotal.childCount - 1) {
            val tvTotal = llTotal.getChildAt(column + 1)
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

    private fun checkTotalButtonColor(button: Button, dateToday: String) {
        val lastValue = getLastTotal(button, dateToday)
        val value = getValue(button)

        when {
            value > lastValue -> button.setTextColor(Color.RED)
            value < lastValue -> button.setTextColor(Color.GREEN)
            else -> button.setTextColor(Color.BLACK)
        }
    }

    private fun getLastAmountEditText(dateToday: String, row: Int): EditText? {
        val column = searchColumn(dateToday)
        if (column != -1 && column < llTotal.childCount - 1) {
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

    private fun calculateTotal(editText: EditText, s: Editable?, date: String, row: Int) {
        var total = 0.0
        val column = searchColumn(date)
        if (column >= 0) {
            for (i in 0 until tableRows.size - 1) {
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
        val btnTotal = llTotal.getChildAt(column)
        if (btnTotal is Button) {
            btnTotal.text = DigitUtils.getRound(total, 2)
            checkTotalButtonColor(btnTotal, date)
        }

        val lastValue = getLastAmount(editText, date, row)
        val value = getValue(editText)

        when {
            value > lastValue -> editText.setTextColor(Color.RED)
            value < lastValue -> editText.setTextColor(Color.GREEN)
            else -> editText.setTextColor(Color.BLACK)
        }

        val account = getAccount(row)
        checkSaveMenu(value, account, date)
    }

    private fun getAccount(row: Int): String {
        if (row < 0 || row >= llAccount.childCount) {
            return ""
        }
        val tvAccount = llAccount.getChildAt(row)
        if (tvAccount is TextView) {
            return tvAccount.text.toString()
        }
        return ""
    }

    private fun checkSaveMenu(value: Double, account: String, date: String) {
        doAsync {

            val fortune = FortuneDatabase.getDatabase(this@MainActivity).getFortuneDao().query(account, date)
            if (fortune == null) {
                updateSaveMenu(true)
                return@doAsync
            }
            updateSaveMenu(value != fortune.fortune)
        }
    }

    private fun showDifferenceValue(value: Double, lastValue: Double) {
        val sign = if (value >= lastValue) "+" else "-"
        val absolute = DigitUtils.getRound(Math.abs(value - lastValue), 2)
        toast("$sign $absolute")
    }

    private fun newAmountEditText(date: String, row: Int): EditText {
        val editText = EditText(this)
        editText.setSingleLine()
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val filters = arrayOf<InputFilter>(CashierInputFilter())
        editText.filters = filters

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
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_UP) {
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
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        return editText
    }
}