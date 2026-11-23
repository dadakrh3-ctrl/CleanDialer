package com.cleandialer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val Bg = Color(0xFF0F1115)
private val Surface1 = Color(0xFF1A1D23)
private val Accent = Color(0xFF34D17B)
private val TextMain = Color(0xFFF2F3F5)
private val TextDim = Color(0xFF8A8F98)
private val Missed = Color(0xFFFF6B6B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Bg.toArgb()
        window.navigationBarColor = Bg.toArgb()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Bg,
                    surface = Surface1,
                    primary = Accent,
                    onBackground = TextMain,
                    onSurface = TextMain
                )
            ) {
                DialerApp()
            }
        }
    }
}

@Composable
fun DialerApp() {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var number by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(0) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refresh++ }

    fun call(n: String) {
        if (n.isBlank()) return
        if (hasPerm(ctx, Manifest.permission.CALL_PHONE)) {
            ctx.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", n, null)))
        } else {
            permLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG))
            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", n, null)))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(Modifier.weight(1f)) {
            if (tab == 0) {
                Keypad(
                    number = number,
                    onDigit = { number += it },
                    onBackspace = { if (number.isNotEmpty()) number = number.dropLast(1) },
                    onClear = { number = "" },
                    onCall = { call(number) }
                )
            } else {
                Recents(
                    refreshKey = refresh,
                    onRequestPermission = {
                        permLauncher.launch(
                            arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG)
                        )
                    },
                    onCall = { call(it) }
                )
            }
        }
        BottomTabs(tab) { tab = it }
    }
}

@Composable
private fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("Keypad", "Recents").forEachIndexed { i, label ->
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (selected == i) Accent else TextDim,
                    fontSize = 15.sp,
                    fontWeight = if (selected == i) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private val keys = listOf(
    "1" to "", "2" to "ABC", "3" to "DEF",
    "4" to "GHI", "5" to "JKL", "6" to "MNO",
    "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
    "*" to "", "0" to "+", "#" to ""
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Keypad(
    number: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCall: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = TextMain,
                fontSize = if (number.length > 12) 28.sp else 40.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        for (r in 0 until 4) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (c in 0 until 3) {
                    val (digit, letters) = keys[r * 3 + c]
                    Box(
                        Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(Surface1)
                            .combinedClickable(
                                onClick = { onDigit(digit) },
                                onLongClick = { if (digit == "0") onDigit("+") }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(digit, color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Light)
                            if (letters.isNotEmpty()) {
                                Text(letters, color = TextDim, fontSize = 10.sp, letterSpacing = 1.5.sp)
                            }
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(78.dp))
            Box(
                Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(if (number.isBlank()) Accent.copy(alpha = 0.35f) else Accent)
                    .clickable { onCall() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Call", tint = Bg, modifier = Modifier.size(32.dp))
            }
            Box(
                Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .combinedClickable(onClick = onBackspace, onLongClick = onClear),
                contentAlignment = Alignment.Center
            ) {
                if (number.isNotEmpty()) {
                    Text("⌫", color = TextDim, fontSize = 26.sp)
                }
            }
        }
    }
}

private data class CallEntry(val number: String, val name: String?, val type: Int, val date: Long)

@Composable
private fun Recents(refreshKey: Int, onRequestPermission: () -> Unit, onCall: (String) -> Unit) {
    val ctx = LocalContext.current
    val granted = hasPerm(ctx, Manifest.permission.READ_CALL_LOG)
    val entries = remember(refreshKey, granted) { if (granted) loadCalls(ctx) else emptyList() }

    if (!granted) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("See your recent calls", color = TextMain, fontSize = 20.sp)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(Accent)
                    .clickable { onRequestPermission() }
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text("Allow", color = Bg, fontWeight = FontWeight.SemiBold)
            }
        }
        return
    }

    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recent calls", color = TextDim, fontSize = 16.sp)
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Text(
                "Recents",
                color = TextMain,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )
        }
        items(entries) { e ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onCall(e.number) }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        e.name?.takeIf { it.isNotBlank() } ?: e.number,
                        color = if (e.type == CallLog.Calls.MISSED_TYPE) Missed else TextMain,
                        fontSize = 17.sp
                    )
                    Text(
                        when (e.type) {
                            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                            CallLog.Calls.MISSED_TYPE -> "Missed"
                            else -> "Incoming"
                        },
                        color = TextDim,
                        fontSize = 13.sp
                    )
                }
                Text(
                    DateUtils.getRelativeTimeSpanString(
                        e.date, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    color = TextDim,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun hasPerm(ctx: Context, perm: String): Boolean =
    ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED

private fun loadCalls(ctx: Context): List<CallEntry> {
    val out = mutableListOf<CallEntry>()
    try {
        ctx.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
            ),
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )?.use { c ->
            while (c.moveToNext() && out.size < 60) {
                val number = c.getString(0) ?: continue
                out.add(CallEntry(number, c.getString(1), c.getInt(2), c.getLong(3)))
            }
        }
    } catch (_: SecurityException) {
    }
    return out
}
