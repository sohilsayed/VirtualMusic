package com.canopus.Vmusic.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canopus.Vmusic.util.DynamicTheme
import com.canopus.Vmusic.util.PaletteExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FullPlayerViewModel @Inject constructor(
    private val paletteExtractor: PaletteExtractor
) : ViewModel() {

    private val _dynamicTheme = MutableStateFlow(DynamicTheme.default(Color.Black, Color.White))
    val dynamicTheme: StateFlow<DynamicTheme> = _dynamicTheme.asStateFlow()

    fun updateThemeFromArtwork(artworkUri: String?) {
        viewModelScope.launch {
            val defaultTheme = DynamicTheme.default(
                defaultPrimary = _dynamicTheme.value.primary,
                defaultOnPrimary = _dynamicTheme.value.onPrimary
            )
            _dynamicTheme.value = paletteExtractor.extractThemeFromUrl(artworkUri, defaultTheme)
        }
    }
}