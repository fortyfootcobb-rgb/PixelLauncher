package com.mike.pixellauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppEntry(
    val label: String,
    val packageName: String,
    val componentName: ComponentName,
    val iconBitmap: Bitmap?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PixelLauncherScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PixelLauncherScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pixel_launcher_prefs", Context.MODE_PRIVATE) }
    val iconPackManager = remember { IconPackManager(context) }

    var selectedIconPack by remember { mutableStateOf(prefs.getString("selected_
cat << 'EOF' > ~/PixelLauncher/app/src/main/java/com/mike/pixellauncher/MainActivity.kt
package com.mike.pixellauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppEntry(
    val label: String,
    val packageName: String,
    val componentName: ComponentName,
    val iconBitmap: Bitmap?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PixelLauncherScreen()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PixelLauncherScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("pixel_launcher_prefs", Context.MODE_PRIVATE) }
    val iconPackManager = remember { IconPackManager(context) }

    var selectedIconPack by remember { mutableStateOf(prefs.getString("selected_icon_pack", null)) }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var showIconPackDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppMenu by remember { mutableStateOf<AppEntry?>(null) }

    LaunchedEffect(selectedIconPack) {
        scope.launch {
            val loaded = withContext(Dispatchers.IO) {
                iconPackManager.loadIconPack(selectedIconPack)
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                launcherApps.getActivityList(null, Process.myUserHandle())
                    .filter { it.componentName.packageName != context.packageName }
                    .map { info ->
                        val customIcon = iconPackManager.getCustomIconForComponent(info.componentName)
                        val fallbackIcon = IconPackManager.drawableToBitmap(info.getBadgedIcon(0))
                        AppEntry(
                            label = info.label.toString(),
                            packageName = info.componentName.packageName,
                            componentName = info.componentName,
                            iconBitmap = customIcon ?: fallbackIcon
                        )
                    }
                    .sortedBy { it.label.lowercase() }
            }
            apps = loaded
        }
    }

    val currentDate = remember { SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(onClick = {}, onLongClick = { showIconPackDialog = true })
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -35f) isDrawerOpen = true
                }
            }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(currentDate, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = if (selectedIconPack == null) "Stock Pixel Icons • Long-press for Icon Packs"
                        else "Custom Icon Pack Active • Long-press to switch",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 12.sp
                    )
                }
                FilledTonalButton(onClick = { showIconPackDialog = true }, shape = RoundedCornerShape(20.dp)) {
                    Text("Icons", fontSize = 13.sp)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps.take(15), key = { it.componentName.flattenToString() }) { app ->
                    AppIconTile(
                        app = app,
                        showLabel = true,
                        onClick = { launchApp(context, app) },
                        onLongClick = { selectedAppMenu = app }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⌃ Swipe up or tap for all apps",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { isDrawerOpen = true }.padding(12.dp, 6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                        .background(Color.Black.copy(alpha = 0.28f)).padding(vertical = 10.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    apps.take(5).forEach { app ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            AppIconTile(app = app, showLabel = false, onClick = { launchApp(context, app) }, onLongClick = { selectedAppMenu = app })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().height(50.dp).clickable { isDrawerOpen = true },
                    shape = CircleShape,
                    color = Color(0xFF282A2E).copy(alpha = 0.92f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("G   Search apps...", color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
                        Text("${apps.size} apps", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                    }
                }
            }
        }

        if (isDrawerOpen) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF141518).copy(alpha = 0.97f)) {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search your Pixel apps...") },
                            singleLine = true,
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { searchQuery = ""; isDrawerOpen = false }) { Text("Close") }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val filtered = remember(apps, searchQuery) {
                        if (searchQuery.isBlank()) apps else apps.filter { it.label.contains(searchQuery.trim(), true) }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.componentName.flattenToString() }) { app ->
                            AppIconTile(
                                app = app,
                                showLabel = true,
                                onClick = { isDrawerOpen = false; searchQuery = ""; launchApp(context, app) },
                                onLongClick = { selectedAppMenu = app }
                            )
                        }
                    }
                }
            }
        }

        if (showIconPackDialog) {
            val installedPacks = remember(showIconPackDialog) { iconPackManager.getAvailableIconPacks() }
            AlertDialog(
                onDismissRequest = { showIconPackDialog = false },
                title = { Text("Select Icon Pack") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedIconPack == null) Color(0xFF2B4C7E) else Color(0xFF24262B),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        prefs.edit().remove("selected_icon_pack").apply()
                                        selectedIconPack = null
                                        showIconPackDialog = false
                                    }
                                ) {
                                    Text("✓ Stock Pixel Icons (Default)", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Medium)
                                }
                            }
                            items(installedPacks) { pack ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedIconPack == pack.packageName) Color(0xFF2B4C7E) else Color(0xFF24262B),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        prefs.edit().putString("selected_icon_pack", pack.packageName).apply()
                                        selectedIconPack = pack.packageName
                                        showIconPackDialog = false
                                    }
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        pack.icon?.let { bmp ->
                                            Image(bitmap = bmp.asImageBitmap(), contentDescription = pack.label, modifier = Modifier.size(36.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }
                                        Column {
                                            Text(pack.label, fontWeight = FontWeight.Medium)
                                            Text(pack.packageName, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showIconPackDialog = false
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=icon%20pack&c=apps")))
                    }) { Text("Get Packs on Play Store") }
                },
                dismissButton = {
                    TextButton(onClick = { showIconPackDialog = false }) { Text("Done") }
                }
            )
        }

        selectedAppMenu?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppMenu = null },
                title = { Text(app.label) },
                text = { Text("Package: ${app.packageName}") },
                confirmButton = {
                    Button(onClick = {
                        val infoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}"))
                        infoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(infoIntent)
                        selectedAppMenu = null
                    }) { Text("App Info") }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAppMenu = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconTile(app: AppEntry, showLabel: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(14.dp)).combinedClickable(onClick = onClick, onLongClick = onLongClick).padding(4.dp, 2.dp)
    ) {
        app.iconBitmap?.let { bmp ->
            Image(bitmap = bmp.asImageBitmap(), contentDescription = app.label, contentScale = ContentScale.Fit, modifier = Modifier.size(54.dp))
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(app.label, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

private fun launchApp(context: Context, app: AppEntry) {
    try {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        launcherApps.startMainActivity(app.componentName, Process.myUserHandle(), null, null)
    } catch (_: Exception) {
        context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}
