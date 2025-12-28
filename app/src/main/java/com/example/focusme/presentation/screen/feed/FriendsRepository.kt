package com.example.focusme.presentation.screen.feed

import com.example.focusme.data.db.FriendEntity
import com.example.focusme.data.db.FriendsDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FriendsRepository(
    private val dao: FriendsDao
) {

    // ✅ Fake data (API simulée)
    private val users = listOf(
        UserDto(1, "Rahma Borsali", "rahma"),
        UserDto(2, "Takwa Borsali", "takwa"),
        UserDto(3, "Eya Hammami", "eya"),
        UserDto(4, "Tasnim Gmati", "tasnim"),
        UserDto(5, "Maryem Anane", "maryem")
    )
    suspend fun removeFriend(id: Int) {
        dao.deleteById(id)
    }
    // ✅ “API calls” simulés + check Room (amis déjà ajoutés)
    suspend fun searchUsers(query: String): List<UserDto> {
        delay(400) // simule API call
        val q = query.lowercase().trim()
        val friendIds = dao.getFriendIds().toSet()

        return users
            .filter { it.name.lowercase().contains(q) || it.username.lowercase().contains(q) }
            .map { it.copy(isFriend = friendIds.contains(it.id)) }
    }

    suspend fun addFriend(user: UserDto) {
        delay(200) // simule API call
        dao.upsert(FriendEntity(id = user.id, name = user.name, username = user.username))
    }

    fun observeFriends(): Flow<List<UserDto>> {
        return dao.observeFriends().map { list ->
            list.map { UserDto(it.id, it.name, it.username, isFriend = true) }
        }
    }
}
