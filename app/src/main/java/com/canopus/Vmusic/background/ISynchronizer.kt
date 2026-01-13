package com.canopus.Vmusic.background

interface ISynchronizer {
    
    val name: String

    
    suspend fun synchronize(): Boolean
}