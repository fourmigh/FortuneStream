package org.caojun.fortunestream.room

import android.arch.persistence.room.*

/**
 * Created by CaoJun on 2017/9/25.
 */
@Dao
interface DateDao {
    @Query("SELECT * FROM date")
    fun queryAll(): List<Date>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg account: Date)

    @Delete
    fun delete(vararg account: Date): Int
}