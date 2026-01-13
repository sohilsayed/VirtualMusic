package com.canopus.Vmusic.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.canopus.Vmusic.BuildConfig
import com.canopus.Vmusic.R
import com.canopus.Vmusic.auth.AuthState
import com.canopus.Vmusic.auth.AuthViewModel
import com.canopus.Vmusic.data.AppPreferenceConstants
import com.canopus.Vmusic.data.ThemePreference
import com.canopus.Vmusic.ui.composables.ApiKeyInputScreen
import com.canopus.Vmusic.ui.dialogs.AddExternalChannelDialog
import com.canopus.Vmusic.ui.navigation.AppDestinations
import com.canopus.Vmusic.viewmodel.ScanStatus
import com.canopus.Vmusic.viewmodel.SettingsSideEffect
import com.canopus.Vmusic.viewmodel.SettingsViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun SettingsScreen(
    navController: NavController,
    onNavigateUp: () -> Unit,
    onApiKeySavedRestartNeeded: () -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val state by settingsViewModel.collectAsState()

    var showAddChannelDialog by remember { mutableStateOf(false) }

    if (showAddChannelDialog) {
        AddExternalChannelDialog(
            onDismissRequest = { showAddChannelDialog = false }
        )
    }


    var isClearingCache by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri != null) {
                settingsViewModel.saveDownloadLocation(uri)
            }
        }
    )
    var showRestartMessageForDataSettings by remember { mutableStateOf(false) }

    settingsViewModel.collectSideEffect { effect ->
        when (effect) {
            is SettingsSideEffect.ShowToast -> Toast.makeText(
                context,
                effect.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(state.cacheClearStatus) {
        state.cacheClearStatus?.let { status ->
            Toast.makeText(context, status, Toast.LENGTH_LONG).show()
            settingsViewModel.resetCacheClearStatus()
            isClearingCache = false
        }
    }

    LaunchedEffect(state.scanStatus) {
        when (val status = state.scanStatus) {
            is ScanStatus.Complete -> {
                val message =
                    if (status.importedCount > 0) "Successfully imported ${status.importedCount} file(s)!" else "Scan complete. No new files found."
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                settingsViewModel.resetScanStatus()
            }

            is ScanStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                settingsViewModel.resetScanStatus()
            }

            else -> {}
        }
    }

    if (showRestartMessageForDataSettings) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Settings apply after app restart.", Toast.LENGTH_LONG).show()
            showRestartMessageForDataSettings = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ... (API Key Section - Keep as is) ...
            SettingsSectionTitle(stringResource(R.string.settings_section_api_key))
            ApiKeyInputScreen(
                settingsViewModel = settingsViewModel,
                onApiKeySavedSuccessfully = { onApiKeySavedRestartNeeded() },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider()

            SettingsSectionTitle("Content Sources")

            ListItem(
                headlineContent = { Text("Add YouTube Channel") },
                supportingContent = {
                    Text(
                        "Import music from external YouTube channels.",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.clickable {
                    showAddChannelDialog = true // Open Dialog
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider()

            SettingsSectionTitle(stringResource(R.string.settings_section_account))
            when (val s = authState) {
                is AuthState.LoggedIn -> {
                    ListItem(
                        headlineContent = { Text("Logged In") },
                        supportingContent = { Text("Your data is being synchronized.") },
                        leadingContent = { Icon(Icons.Default.CloudSync, null) },
                        trailingContent = {
                            TextButton(onClick = { authViewModel.logout() }) {
                                Text(
                                    stringResource(R.string.action_logout)
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    Button(
                        onClick = { settingsViewModel.triggerManualSync() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Sync Now")
                    }
                }

                is AuthState.LoggedOut, is AuthState.Error -> {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.action_login)) },
                        supportingContent = { Text(stringResource(R.string.settings_desc_login)) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Login, null) },
                        modifier = Modifier.clickable { navController.navigate(AppDestinations.LOGIN_ROUTE) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    if (s is AuthState.Error) {
                        Text(
                            "Login failed: ${s.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                is AuthState.InProgress -> {
                    ListItem(
                        headlineContent = { Text("Logging in...") },
                        leadingContent = { CircularProgressIndicator(modifier = Modifier.size(24.dp)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            HorizontalDivider()
            SettingsSectionTitle(stringResource(R.string.settings_section_playback))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_label_autoplay_next_video)) },
                supportingContent = {
                    Text(
                        stringResource(R.string.settings_desc_autoplay_next_video),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                trailingContent = {
                    Switch(
                        checked = state.autoplayEnabled,
                        onCheckedChange = { settingsViewModel.setAutoplayNextVideoEnabled(it) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsViewModel.setAutoplayNextVideoEnabled(!state.autoplayEnabled) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_label_shuffle_on_play)) },
                supportingContent = {
                    Text(
                        stringResource(R.string.settings_desc_shuffle_on_play),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                trailingContent = {
                    Switch(
                        checked = state.shuffleOnPlayStartEnabled,
                        onCheckedChange = { settingsViewModel.setShuffleOnPlayStartEnabled(it) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsViewModel.setShuffleOnPlayStartEnabled(!state.shuffleOnPlayStartEnabled) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()


            PreferenceGroupTitle(stringResource(R.string.settings_label_download_location))
            ListItem(
                headlineContent = {
                    Text(
                        if (state.downloadLocation.isEmpty()) stringResource(R.string.settings_download_location_default) else state.downloadLocation,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = { Text(stringResource(R.string.settings_desc_download_location)) },
                leadingContent = { Icon(Icons.Default.FolderOpen, null) },
                modifier = Modifier.clickable {
                    try {
                        folderPickerLauncher.launch(null)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error opening picker", Toast.LENGTH_SHORT).show()
                    }
                },
                trailingContent = {
                    if (state.downloadLocation.isNotEmpty()) {
                        IconButton(onClick = { settingsViewModel.clearDownloadLocation() }) {
                            Icon(
                                Icons.Default.Clear,
                                stringResource(R.string.action_clear_location)
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            PreferenceGroupTitle(stringResource(R.string.settings_label_image_quality))
            PreferenceRadioGroup {
                ImageQualityOptions.entries.forEach { quality ->
                    PreferenceRadioButton(
                        text = quality.displayName,
                        selected = state.currentImageQuality == quality.key,
                        onClick = {
                            settingsViewModel.setImageQualityPreference(quality.key)
                            if (quality.key != AppPreferenceConstants.IMAGE_QUALITY_AUTO) showRestartMessageForDataSettings =
                                true
                        }
                    )
                }
            }
            PreferenceDescription(stringResource(R.string.settings_desc_image_quality))

            PreferenceGroupTitle(stringResource(R.string.settings_label_audio_quality))
            PreferenceRadioGroup {
                AudioQualityOptions.entries.forEach { quality ->
                    PreferenceRadioButton(
                        text = quality.displayName,
                        selected = state.currentAudioQuality == quality.key,
                        onClick = { settingsViewModel.setAudioQualityPreference(quality.key) }
                    )
                }
            }
            PreferenceDescription(stringResource(R.string.settings_desc_audio_quality))

            PreferenceGroupTitle(stringResource(R.string.settings_label_list_loading_config))
            PreferenceRadioGroup {
                ListLoadingConfigOptions.entries.forEach { config ->
                    PreferenceRadioButton(
                        text = config.displayName,
                        selected = state.currentListLoadingConfig == config.key,
                        onClick = {
                            settingsViewModel.setListLoadingConfigPreference(config.key)
                            showRestartMessageForDataSettings = true
                        }
                    )
                }
            }
            PreferenceDescription(stringResource(R.string.settings_desc_list_loading_config))

            PreferenceGroupTitle(stringResource(R.string.settings_label_buffering_strategy))
            PreferenceRadioGroup {
                BufferingStrategyOptions.entries.forEach { strategy ->
                    PreferenceRadioButton(
                        text = strategy.displayName,
                        selected = state.currentBufferingStrategy == strategy.key,
                        onClick = {
                            settingsViewModel.setBufferingStrategyPreference(strategy.key)
                            showRestartMessageForDataSettings = true
                        }
                    )
                }
            }
            PreferenceDescription(stringResource(R.string.settings_desc_buffering_strategy))

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            SettingsSectionTitle(stringResource(R.string.settings_section_cache))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Button(
                    onClick = {
                        isClearingCache = true; settingsViewModel.clearAllApplicationCaches()
                    },
                    enabled = !isClearingCache,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.settings_button_clear_cache))
                }
                if (isClearingCache) CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            }
            Text(
                stringResource(R.string.settings_desc_clear_cache),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            HorizontalDivider()

            SettingsSectionTitle(stringResource(R.string.settings_section_theme))
            PreferenceRadioGroup {
                ThemePreferenceOptions.entries.forEach { themeOpt ->
                    PreferenceRadioButton(
                        text = themeOpt.displayName,
                        selected = state.currentTheme == themeOpt.key,
                        onClick = { settingsViewModel.setThemePreference(themeOpt.key) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            SettingsSectionTitle("About Vmusic")

            val uriHandler = LocalUriHandler.current

            ListItem(
                headlineContent = { Text("Version ${BuildConfig.VERSION_NAME}") },
                supportingContent = { Text("A specialized music client for the Vtuber community.") },
                leadingContent = { Icon(Icons.Default.Info, null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Make sure you have ic_holodex_logo in res/drawable
                        Image(
                            painter = painterResource(id = R.drawable.ic_holodex_logo),
                            contentDescription = "Holodex Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Powered by Holodex",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Vmusic uses the Holodex and Musicdex APIs to provide metadata, song segments, and discovery features. This project is a third-party client and is not affiliated with the Holodex team.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { uriHandler.openUri("https://holodex.net") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Visit Holodex.net")
                    }
                }
            }

            ListItem(
                headlineContent = { Text("NewPipe Extractor") },
                supportingContent = {
                    Text("Powered by the NewPipe team's open-source library for reliable stream handling.")
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stat_music_note), // Or a Github icon
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingContent = {
                    IconButton(onClick = {
                        uriHandler.openUri("https://github.com/TeamNewPipe/NewPipeExtractor")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, "GitHub")
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun PreferenceGroupTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun PreferenceDescription(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun PreferenceRadioGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.selectableGroup()) { content() }
}

@Composable
private fun PreferenceRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        leadingContent = { RadioButton(selected = selected, onClick = null, enabled = enabled) },
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = if (enabled) onClick else ({}),
                role = Role.RadioButton,
                enabled = enabled
            ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

enum class ImageQualityOptions(val key: String, val displayName: String) {
    AUTO(AppPreferenceConstants.IMAGE_QUALITY_AUTO, "Auto (Recommended)"),
    MEDIUM(AppPreferenceConstants.IMAGE_QUALITY_MEDIUM, "Medium (Faster loading)"),
    LOW(AppPreferenceConstants.IMAGE_QUALITY_LOW, "Low (Data saver)")
}

enum class AudioQualityOptions(val key: String, val displayName: String) {
    BEST(AppPreferenceConstants.AUDIO_QUALITY_BEST, "Best Available"),
    STANDARD(AppPreferenceConstants.AUDIO_QUALITY_STANDARD, "Standard (~128kbps)"),
    SAVER(AppPreferenceConstants.AUDIO_QUALITY_SAVER, "Data Saver (~64kbps)")
}

enum class ListLoadingConfigOptions(val key: String, val displayName: String) {
    NORMAL(AppPreferenceConstants.LIST_LOADING_NORMAL, "Normal (Smooth scrolling)"),
    REDUCED(AppPreferenceConstants.LIST_LOADING_REDUCED, "Reduced (Less data, faster initial)"),
    MINIMAL(AppPreferenceConstants.LIST_LOADING_MINIMAL, "Minimal (Data saver, slowest scroll)")
}

enum class BufferingStrategyOptions(val key: String, val displayName: String) {
    AGGRESSIVE(AppPreferenceConstants.BUFFERING_STRATEGY_AGGRESSIVE, "Quick Start (Default)"),
    BALANCED(AppPreferenceConstants.BUFFERING_STRATEGY_BALANCED, "Balanced"),
    STABLE(AppPreferenceConstants.BUFFERING_STRATEGY_STABLE, "Stable Playback (More buffering)")
}

enum class ThemePreferenceOptions(val key: String, val displayName: String) {
    LIGHT(ThemePreference.LIGHT, "Light"),
    DARK(ThemePreference.DARK, "Dark"),
    SYSTEM(ThemePreference.SYSTEM, "Follow System")
}