package com.canopus.Vmusic.data.repository

import com.canopus.Vmusic.data.db.SearchHistoryDao
import com.canopus.Vmusic.data.db.SearchHistoryEntity
import com.canopus.Vmusic.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomSearchHistoryRepository @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchHistoryRepository {

    // Matches the interface Flow<List<String>>
    override val searchHistory: Flow<List<String>> = searchHistoryDao.getSearchHistory()

    override suspend fun addSearchQueryToHistory(query: String) {
        withContext(ioDispatcher) {
            searchHistoryDao.insert(SearchHistoryEntity(query = query))
        }
    }

    override suspend fun loadSearchHistory() {
        // Room Flow is reactive and loads automatically; no-op.
    }

    override suspend fun clearSearchHistory() {
        withContext(ioDispatcher) {
            searchHistoryDao.clearAll()
        }
    }
}