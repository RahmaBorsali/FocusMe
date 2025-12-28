package com.example.focusme.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(friend: FriendEntity)

    @Query("SELECT * FROM friends ORDER BY name ASC")
    fun observeFriends(): Flow<List<FriendEntity>>

    @Query("SELECT id FROM friends")
    suspend fun getFriendIds(): List<Int>

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteById(id: Int)



}
