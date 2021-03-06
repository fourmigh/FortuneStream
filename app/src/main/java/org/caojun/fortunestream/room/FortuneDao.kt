package org.caojun.fortunestream.room

import android.arch.persistence.room.*

/**
 * Created by CaoJun on 2017/9/25.
 */
@Dao
interface FortuneDao {
    @Query("SELECT * FROM fortune")
    fun queryAll(): List<Fortune>

    @Query("SELECT * FROM fortune WHERE fortune.account = :account AND fortune.date = :date")
    fun query(account: String, date: String): Fortune?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg account: Fortune)

    @Delete
    fun delete(vararg account: Fortune): Int
}