package org.caojun.fortunestream.room

import android.arch.persistence.room.*

/**
 * Created by CaoJun on 2017/9/25.
 */
@Dao
interface AccountDao {
    @Query("SELECT * FROM account")
    fun queryAll(): List<Account>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg account: Account)

    @Delete
    fun delete(vararg account: Account): Int
}