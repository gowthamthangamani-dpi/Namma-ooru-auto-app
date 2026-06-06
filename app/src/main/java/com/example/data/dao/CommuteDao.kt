package com.example.data.dao

import androidx.room.*
import com.example.data.model.FrequentCommute
import kotlinx.coroutines.flow.Flow

@Dao
interface CommuteDao {
    @Query("SELECT * FROM frequent_commutes ORDER BY id ASC")
    fun getAllCommutes(): Flow<List<FrequentCommute>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommute(commute: FrequentCommute)

    @Delete
    suspend fun deleteCommute(commute: FrequentCommute)

    @Query("SELECT COUNT(*) FROM frequent_commutes")
    suspend fun getCount(): Int
}
